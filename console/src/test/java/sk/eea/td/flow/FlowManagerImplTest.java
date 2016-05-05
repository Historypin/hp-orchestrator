package sk.eea.td.flow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

public class FlowManagerImplTest {

    private static final int N = 10;

    private static CountDownLatch startSignal = new CountDownLatch(1);

    private static CountDownLatch threadsCreatedSignal = new CountDownLatch(10);

    @InjectMocks
    private FlowManagerImpl flowManagerImpl = new FlowManagerImpl();

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobRunRepository jobRunRepository;

    @Mock
    private ParamRepository paramRepository;

    @Mock
    private LogRepository logRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doAnswer(new Answer<Job>() {
            @Override
            public Job answer(InvocationOnMock invocation) throws Throwable {
                Job job = new Job();
                Thread.sleep(500); // simulate long running DB job
                return job;
            }
        }).when(jobRepository).findFirstByLastJobRunIsNullAndSourceIsInOrderByIdAsc(anyList());
        when(paramRepository.findByJob(any(Job.class))).thenReturn(new HashSet<>());
        JobRun jobRun = new JobRun();
        when(jobRunRepository.save(any(JobRun.class))).thenReturn(jobRun);

    }

    @Test
    public void testTrigger() throws Exception {
        for (int i = 0; i < N; i++) { // create and start threads
            new Thread(() -> {
                try {
                    threadsCreatedSignal.countDown();
                    startSignal.await(); // wait for signal
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                flowManagerImpl.trigger(); // go for trigger
            }
            ).start();
        }

        threadsCreatedSignal.await(); // wait for all threads are created
        startSignal.countDown();

        // verify that only one thread had created its run
        verify(jobRepository, timeout(1000).times(1)).findFirstByLastJobRunIsNullAndSourceIsInOrderByIdAsc(anyList());
        verify(jobRunRepository, timeout(1000).times(2)).save((JobRun) any());
        verify(paramRepository, timeout(1000).times(1)).findByJob(any());
        verify(jobRepository, timeout(1000).times(1)).save((Job) any());
        verify(logRepository, timeout(1000).times(0)).save((Log) any());
    }
}
