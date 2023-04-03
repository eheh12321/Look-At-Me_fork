package com.lookatme.server.config;

import com.lookatme.server.job.JobCompletionListener;
import com.lookatme.server.job.SomeBatchJobWithAnnotation;
import com.lookatme.server.job.SomeBatchJobWithTaskletAdapter;
import com.lookatme.server.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Batch에서는 3개의 Sequence Table, 6개의 Meta Table이 존재
 * # Sequence Table
 * - BATCH_JOB_SEQ
 * - BATCH_JOB_EXECUTION_SEQ
 * - BATCH_STEP_EXECUTION_SEQ
 * # Meta Table
 * - BATCH_JOB_INSTANCE: JobInstance에 관한 정보 저장
 * - BATCH_JOB_EXECUTION: JobExecution에 관한 정보 저장(시작시간, 종료시간, 종료코드 등...)
 * - BATCH_JOB_EXECUTION_PARAMS: Job을 실행시킬 때 사용되는 JobParameters에 대한 정보 저장
 * - BATCH_JOB_EXECUTION_CONTEXT: JobExecution의 ExecutionContext 정보 저장(JobInstance 실패 시 중단된 위치에서 재시작 될 수 있도록)
 * - BATCH_STEP_EXECUTION: StepExecution에 관한 정보 저장(읽은 수, 커밋 수, 스킵 수 등...)
 * - BATCH_STEP_EXECUTION_CONTEXT: StepExecution의 ExecutionContext 정보 저장(JobInstance의 Step 실패 시 중단된 위치에서 재시작 될 수 있도록)
 */
@Slf4j
@RequiredArgsConstructor
@EnableBatchProcessing
@Configuration // 모든 Job은 Configuration을 통해 스프링 빈으로 등록해서 사용
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job simpleJob(JobCompletionListener listener) {
        /**
         * Job: 하나의 배치 작업 단위
         * - 하나의 Job에 여러개의 Step이 들어가는 구조
         */
        return jobBuilderFactory.get("simpleJob")
                .start(simpleStep())
                .next(simpleStep2())
                .next(simpleStep3())
                .listener(listener)
                .build();
    }

    @Bean
    public Step simpleStep() {
        /**
         * Job은 최소 1개 이상의 Step을 가진다
         * - Step이 실행될 때 StepExecution이 생성됨
         * - 이전 단계의 Step이 실패할 경우 다음 단계는 실행되지 않음(StepExecution 생성 X)
         */
        return stepBuilderFactory.get("simpleStep")
                .tasklet(((contribution, chunkContext) -> {
                    // Step 내부에서 수행될 기능
                    log.warn(">>>> stepBuilder 내부에 로직 구현");
                    return RepeatStatus.FINISHED;
                })).build();
    }

    @Bean
    public Step simpleStep2() {
        return stepBuilderFactory.get("simpleStep2")
                .tasklet(myTasklet())
                .build();
    }

    @Bean
    public Step simpleStep3() {
        return stepBuilderFactory.get("simpleStep3")
                .tasklet(new SomeBatchJobWithAnnotation())
                .build();
    }

    @Bean
    public Job chunkJob(JobCompletionListener listener) throws Exception {
        return jobBuilderFactory.get("chunkJob")
                .start(chunkStep())
                .listener(listener)
                .build();
    }

    @JobScope
    @Bean
    public Step chunkStep() throws Exception {
        return stepBuilderFactory.get("chunkStep")
                .<Member, CsvFileDto>chunk(10)
                .reader(reader()) // 읽기
                .processor(processor()) // 로직 처리
                .writer(writer()) // 쓰기
                .build();
    }

    @StepScope
    @Bean
    public JpaPagingItemReader<Member> reader() {
        Map<String, Object> params = new HashMap<>();
        params.put("baseTime", LocalDateTime.now().minusMinutes(30));

        return new JpaPagingItemReaderBuilder<Member>()
                .pageSize(10)
                .queryString("select m from Member m where m.lastLoginTime >= :baseTime order by m.memberId")
                .parameterValues(params)
                .entityManagerFactory(entityManagerFactory)
                .name("JpaPagingItemReader")
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Member, CsvFileDto> processor() {
        return member -> new CsvFileDto(
                member.getMemberId(),
                member.getEmail(),
                member.getLastLoginTime()
        );
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<CsvFileDto> writer() throws Exception {
        BeanWrapperFieldExtractor<CsvFileDto> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"blank", "id", "name", "lastLoginDatetime"});

        DelimitedLineAggregator<CsvFileDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(", ");
        lineAggregator.setFieldExtractor(extractor);

        FlatFileItemWriter<CsvFileDto> writer = new FlatFileItemWriterBuilder<CsvFileDto>()
                .name("csvItemWriter")
                .encoding("UTF-8")
                .resource(new FileSystemResource("output/batch.csv"))
                .lineAggregator(lineAggregator)
                .headerCallback(w -> w.write("baseTime, id, email, lastLoginDatetime"))
                .footerCallback(w -> w.write("[ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " ], ----, ----, ----\n"))
                .append(true)
                .build();
        writer.afterPropertiesSet();
        return writer;
    }

    @Getter
    private static class CsvFileDto {
        private final String blank = "";
        private final long id;
        private final String name;
        private final LocalDateTime lastLoginDatetime;

        public CsvFileDto(long id, String name, LocalDateTime lastLoginDatetime) {
            this.id = id;
            this.name = name;
            this.lastLoginDatetime = lastLoginDatetime;
        }
    }

    @Bean
    public Job flowJob() {
        // flowJob -> ExitStatus에 따른 분기 실행
        return jobBuilderFactory.get("flowJob")
                .start(startStep())
                .on("FAILED") // startStep ExitStatus가 FAILED일 경우
                .to(failOverStep()) // 해당 Step 실행
                .on("*") // 해당 ExitStatus 관계없이
                .to(writeStep()) // 해당 Step 실행
                .on("*") // 해당 ExitStatus 관계없이
                .end() // 종료
                .from(startStep()) // startStep의 ExitStatus가 FAILED가 아니고,
                .on("COMPLETED")
                .to(processStep())
                .on("*")
                .to(writeStep())
                .on("*")
                .end()
                .from(startStep()) // startStep의 ExitStatus가 FAILED, COMPLETED가 아니고,
                .on("*") // 모든 경우
                .to(writeStep()) // 해당 Step 실행
                .on("*")
                .end()
                .end()
                .build();
    }

    @Bean
    public SomeBatchJobWithTaskletAdapter batchService() {
        return new SomeBatchJobWithTaskletAdapter();
    }

    @Bean
    public MethodInvokingTaskletAdapter myTasklet() {
        MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();
        adapter.setTargetObject(batchService());
        adapter.setTargetMethod("businessLogic");
        return adapter;
    }

    @Bean
    public Step startStep() {
        return stepBuilderFactory.get("startStep")
                .tasklet((contribution, chunkContext) -> {
                    log.error(">>>> Start Flow Step");
                    int randomNo = (int) (Math.random() * 3);
                    String[] op = new String[]{"COMPLETED", "FAIL", "UNKNOWN"};
                    String result = op[randomNo];
                    switch (result) {
                        case "COMPLETED":
                            contribution.setExitStatus(ExitStatus.COMPLETED);
                            break;
                        case "FAIL":
                            contribution.setExitStatus(ExitStatus.FAILED);
                            break;
                        case "UNKNOWN":
                            contribution.setExitStatus(ExitStatus.UNKNOWN);
                            break;
                    }
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step failOverStep() {
        return stepBuilderFactory.get("nextStep")
                .tasklet((contribution, chunkContext) -> {
                    log.error(">>>> FailOver Step");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step processStep() {
        return stepBuilderFactory.get("processStep")
                .tasklet((contribution, chunkContext) -> {
                    log.error(">>>> Process Step");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step writeStep() {
        return stepBuilderFactory.get("writeStep")
                .tasklet((contribution, chunkContext) -> {
                    log.error(">>>> Write Step");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
