package sk.eea.td.console.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.WebApplicationContext;
import sk.eea.td.IntegrationTest;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;

import java.lang.reflect.Field;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ReviewTestConfig.class})
@WebAppConfiguration
@Transactional
public class ReviewControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private KeyBasedPersistenceTokenService keyBasedPersistenceTokenService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private ReviewController reviewController;

    private String malformedButValidToken;

    private String correctToken;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Job job = new Job();
        jobRepository.save(job);

        JobRun jobRun = new JobRun();
        jobRun.setJob(job);
        jobRun = jobRunRepository.save(jobRun);

        this.malformedButValidToken = keyBasedPersistenceTokenService.allocateToken("not jobRunId").getKey();
        this.correctToken = keyBasedPersistenceTokenService.allocateToken(jobRun.getId().toString()).getKey();
    }

    @Test
    public void testThatReviewWithoutTokenIsUnauthorized() throws Exception {
        this.mockMvc.perform(get("/review")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testThatReviewWithInvalidTokenIsUnauthorized() throws Exception {
        this.mockMvc.perform(get("/review?token=123")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testThatReviewWithValidTokenIsUnauthorized() throws Exception {
        this.mockMvc.perform(get("/review?token=" + malformedButValidToken)).andExpect(status().isUnauthorized());
    }

    @Test
    public void testThatReviewWithExpiredTokenIsUnauthorized() throws Exception {
        Field field = ReflectionUtils.findField(ReviewController.class, "tokenDaysValid");
        ReflectionUtils.makeAccessible(field);
        Integer oldValue = (Integer) field.get(reviewController);
        field.set(reviewController, -1);
        this.mockMvc.perform(get("/review?token=" + correctToken)).andExpect(status().isUnauthorized());
        field.set(reviewController, oldValue);
    }

    @Test
    public void testThatReviewCorrectTokenIsOK() throws Exception {
        this.mockMvc.perform(get("/review?token=" + correctToken)).andExpect(status().isOk());
    }
}
