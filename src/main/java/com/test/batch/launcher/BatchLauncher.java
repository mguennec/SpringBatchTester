package com.test.batch.launcher;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.ExitCodeMapper;
import org.springframework.batch.core.launch.support.SimpleJvmExitCodeMapper;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import com.test.batch.annotations.BatchTest;
import com.test.batch.annotations.utils.BatchTestUtils;

/**
 * Batch launcher.
 * 
 * @author mguennec
 * 
 */
public class BatchLauncher {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(BatchLauncher.class);

    /** Converts the job parameters. */
	private JobParametersConverter jobParametersConverter = new DefaultJobParametersConverter();

    /** Maps exit code. */
	private ExitCodeMapper exitCodeMapper = new SimpleJvmExitCodeMapper();

	public BatchLauncher() {
		// Ne fait rien
	}

	/**
	 * Launch a test described by a method.
	 * 
	 * @param method
	 *            method
	 * @throws Exception
	 *             if something goes wrong or an assertion fails
	 */
	public void run(final Method method) throws Exception {
		if (!BatchTestUtils.isBatchTest(method)) {
			return;
		}
        final ConfigurableApplicationContext ctxt = BatchTestUtils.getApplicationContext(method);

        try {
			run(method, ctxt);
		} finally {
			ctxt.close();
		}
	}


    /**
	 * Launch a batch test.
	 * 
	 * @param method
	 *            method
	 * @param ctxt
	 *            Spring context
	 * @throws Exception
	 *             if something goes wrong or an assertion fails
	 */
    public void run(final Method method, final ConfigurableApplicationContext ctxt) throws Exception {
		// Gets the job in the Spring context
		final String batchName = BatchTestUtils.getBatchName(method);
		if (!StringUtils.hasLength(batchName)) {
			LOGGER.warn("Batch name not specified, batch operation skipped");
			return;
		}
		final Job job = getJob(ctxt, batchName);
		Assert.assertNotNull("Job not found.", job);

		final String exitCode = run(method, ctxt, job);
		final int expectedReturn = BatchTestUtils.getExpectedReturnValue(method);
		if (BatchTest.DEFAULT != expectedReturn) {
			Assert.assertEquals("Return code not expected.", expectedReturn, exitCodeMapper.intValue(exitCode));
		}
	}

	/**
	 * Launch a batch test.
	 * 
	 * @param method
	 *            method
	 * @param ctxt
	 *            Spring context
	 * @param job
	 *            job
	 * @return return code
	 * @throws Exception
	 *             if something goes wrong or an assertion fails
	 */
	private String run(final Method method, final ConfigurableApplicationContext ctxt, final Job job) {
		String exitCode;
		try {
			final JobExecution jobExecution = runJob(method, ctxt, job);
			final BatchStatus expectedStatus = BatchTestUtils.getExpectedStatus(method);
			if (!BatchStatus.UNKNOWN.equals(expectedStatus)) {
				Assert.assertEquals("Batch Status not expected.", expectedStatus, jobExecution.getStatus());
			}
			exitCode = jobExecution.getExitStatus().getExitCode();
		} catch (AssertionError e) {
			throw e;
		} catch (Throwable e) {
			exitCode = ExitStatus.FAILED.getExitCode();
		}
		return exitCode;
	}

	/**
	 * Launch a batch test.
	 * 
	 * @param method
	 *            method
	 * @param ctxt
	 *            Spring context
	 * @param job
	 *            job to launch
	 * @return job execution
	 * @throws Exception
	 *             if something goes wrong or an assertion fails
	 */
	private JobExecution runJob(final Method method, final ConfigurableApplicationContext ctxt, final Job job) throws Exception {
		final String stepName = BatchTestUtils.getStepName(method);
		final Map<String, String> jobParameters = BatchTestUtils.getJobParameters(method);
		return runJob(ctxt, job, stepName, jobParameters);
	}

	/**
	 * Launch a batch test.
	 * 
	 * @param ctxt
	 *            Spring context
	 * @param job
	 *            job to launch
	 * @param stepName
	 *            step name (null or empty to execute the whole job)
	 * @param jobParameters
	 *            job parameters
	 * @return job execution
	 * @throws Exception
	 *             if something goes wrong or an assertion fails
	 */
	private JobExecution runJob(final ConfigurableApplicationContext ctxt, final Job job, final String stepName, final Map<String, String> jobParameters) throws Exception {
		final JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
		final JobParameters jobParams = getParameters(jobLauncherTestUtils, jobParameters);
		jobLauncherTestUtils.setJob(job);
		jobLauncherTestUtils.setJobLauncher(ctxt.getBean(JobLauncher.class));
		jobLauncherTestUtils.setJobRepository(ctxt.getBean(JobRepository.class));

		// Launching the job or just a step if specified
		final JobExecution jobExecution;
		if (!StringUtils.hasLength(stepName)) {
			jobExecution = jobLauncherTestUtils.launchJob(jobParams);
		} else {
			jobExecution = jobLauncherTestUtils.launchStep(stepName, jobParams);
		}
		return jobExecution;
	}

	/**
	 * Creates a {@link JobParameters} containing the given parameters.
	 * 
	 * @param jobLauncherTestUtils
	 *            utility class to launch batch in tests
	 * @param jobParameters
	 *            parameters map
	 * @return the job parameters
	 */
	private JobParameters getParameters(final JobLauncherTestUtils jobLauncherTestUtils, final Map<String, String> jobParameters) {
		final JobParametersBuilder parameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters());
		final Properties props = new Properties();
		for (final Entry<String, String> entry : jobParameters.entrySet()) {
			props.setProperty(entry.getKey(), entry.getValue());
		}
		final Map<String, JobParameter> params = jobParametersConverter.getJobParameters(props).getParameters();
		for (final Entry<String, JobParameter> entry : params.entrySet()) {
			parameters.addParameter(entry.getKey(), entry.getValue());
		}
		final JobParameters jobParams = parameters.toJobParameters();
		return jobParams;
	}

	/**
	 * Launch a batch test.
	 * 
	 * @param batchName
	 *            batch name
	 * @param contexts
	 *            Spring contexts
	 * @param params
	 *            parameters
	 * @return batch return code
	 * @throws Exception
	 *             if the execution is impossible
	 */
	public int run(final String batchName, final String[] contexts, final Map<String, String> params) throws Exception {
		final ConfigurableApplicationContext ctxt = new ClassPathXmlApplicationContext(contexts);
		try {
			final Job job = getJob(ctxt, batchName);

			String exitCode;
			try {
				final JobExecution jobExecution = runJob(ctxt, job, null, params);
				exitCode = jobExecution.getExitStatus().getExitCode();
			} catch (Throwable e) {
				exitCode = ExitStatus.FAILED.getExitCode();
			}
			return exitCodeMapper.intValue(exitCode);
		} finally {
			ctxt.close();
		}

	}

	/**
	 * creates un objet {@link Job} from a spring context and a job name.
	 * 
	 * @param ctxt
	 *            Spring context
	 * @param batchName
	 *            batch name
	 * @return the job
	 */
	private Job getJob(final ConfigurableApplicationContext ctxt, final String batchName) {
		ctxt.getAutowireCapableBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		Assert.assertTrue("Batch name not found", StringUtils.hasLength(batchName));
		final Job job = ctxt.getBean(batchName, Job.class);
		Assert.assertNotNull("Job not found.", job);
		return job;
	}


    public void setJobParametersConverter(JobParametersConverter jobParametersConverter) {
        this.jobParametersConverter = jobParametersConverter;
    }

    public void setExitCodeMapper(ExitCodeMapper exitCodeMapper) {
        this.exitCodeMapper = exitCodeMapper;
    }
}
