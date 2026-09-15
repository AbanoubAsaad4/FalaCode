package com.nobzzy.falacode.controller;

import com.nobzzy.falacode.entity.User;
import com.nobzzy.falacode.repository.MasteryRecordRepository;
import com.nobzzy.falacode.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import com.nobzzy.falacode.dto.MasteryRecordDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MasteryRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MasteryRecordRepository masteryRecordRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        masteryRecordRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .name("Test Learner")
                .email("learner@example.com")
                .password("hashedpassword")
                .build());
    }

    @Nested
    @DisplayName("POST /api/users/{userId}/mastery-records")
    class CreateMasteryRecordTests {

        @Test
        @DisplayName("Should create mastery record successfully when payload is valid")
        void shouldCreateMasteryRecord_WhenValidRequest() throws Exception {
            MasteryRecordDto requestDto = MasteryRecordDto.builder()
                    .topic("loops")
                    .score(40)
                    .userId(testUser.getId())
                    .build();

            mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.topic", is("loops")))
                    .andExpect(jsonPath("$.score", is(40)))
                    .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when topic is blank")
        void shouldReturn400_WhenTopicIsBlank() throws Exception {
            MasteryRecordDto requestDto = MasteryRecordDto.builder()
                    .topic("")
                    .score(40)
                    .userId(testUser.getId())
                    .build();

            mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when score is out of range")
        void shouldReturn400_WhenScoreOutOfRange() throws Exception {
            MasteryRecordDto requestDto = MasteryRecordDto.builder()
                    .topic("loops")
                    .score(150)
                    .userId(testUser.getId())
                    .build();

            mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 Not Found when userId does not exist")
        void shouldReturn404_WhenUserDoesNotExist() throws Exception {
            MasteryRecordDto requestDto = MasteryRecordDto.builder()
                    .topic("loops")
                    .score(40)
                    .userId(999L)
                    .build();

            mockMvc.perform(post("/api/users/{userId}/mastery-records", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/mastery-records and GET /api/mastery-records/{id}")
    class ReadMasteryRecordTests {

        @Test
        @DisplayName("Should return all mastery records")
        void shouldReturnAllMasteryRecords() throws Exception {
            MasteryRecordDto rec1 = MasteryRecordDto.builder().topic("loops").score(40).userId(testUser.getId()).build();
            MasteryRecordDto rec2 = MasteryRecordDto.builder().topic("recursion").score(20).userId(testUser.getId()).build();

            mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(rec1)));

            mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(rec2)));

            mockMvc.perform(get("/api/mastery-records"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(2)))
                    .andExpect(jsonPath("$[0].topic", is("loops")))
                    .andExpect(jsonPath("$[1].topic", is("recursion")));
        }

        @Test
        @DisplayName("Should return mastery record by ID")
        void shouldReturnMasteryRecordById() throws Exception {
            MasteryRecordDto requestDto = MasteryRecordDto.builder()
                    .topic("loops")
                    .score(40)
                    .userId(testUser.getId())
                    .build();

            String response = mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            MasteryRecordDto created = objectMapper.readValue(response, MasteryRecordDto.class);

            mockMvc.perform(get("/api/mastery-records/{id}", created.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                    .andExpect(jsonPath("$.topic", is("loops")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when getting non-existent mastery record ID")
        void shouldReturn404_WhenMasteryRecordNotFound() throws Exception {
            mockMvc.perform(get("/api/mastery-records/{id}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return mastery records by user ID")
        void shouldReturnMasteryRecordsByUserId() throws Exception {
            MasteryRecordDto rec = MasteryRecordDto.builder().topic("loops").score(40).userId(testUser.getId()).build();

            mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(rec)));

            mockMvc.perform(get("/api/users/{userId}/mastery-records", testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(1)))
                    .andExpect(jsonPath("$[0].topic", is("loops")));
        }
    }

    @Nested
    @DisplayName("PUT /api/mastery-records/{id}")
    class UpdateMasteryRecordTests {

        @Test
        @DisplayName("Should update mastery record successfully")
        void shouldUpdateMasteryRecord() throws Exception {
            MasteryRecordDto initialDto = MasteryRecordDto.builder()
                    .topic("loops")
                    .score(30)
                    .userId(testUser.getId())
                    .build();

            String response = mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(initialDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            MasteryRecordDto created = objectMapper.readValue(response, MasteryRecordDto.class);

            MasteryRecordDto updateDto = MasteryRecordDto.builder()
                    .topic("loops")
                    .score(85)
                    .userId(testUser.getId())
                    .build();

            mockMvc.perform(put("/api/mastery-records/{id}", created.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                    .andExpect(jsonPath("$.score", is(85)));
        }
    }

    @Nested
    @DisplayName("POST /api/users/{userId}/mastery-records/progress")
    class RecordProgressTests {

        @Test
        @DisplayName("Should create a new record on first progress update for a topic")
        void shouldCreateRecord_OnFirstProgressUpdate() throws Exception {
            mockMvc.perform(post("/api/users/{userId}/mastery-records/progress", testUser.getId())
                            .param("topic", "recursion")
                            .param("score", "25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topic", is("recursion")))
                    .andExpect(jsonPath("$.score", is(25)));
        }

        @Test
        @DisplayName("Should update the existing record on a repeat progress update for the same topic")
        void shouldUpdateRecord_OnRepeatProgressUpdate() throws Exception {
            mockMvc.perform(post("/api/users/{userId}/mastery-records/progress", testUser.getId())
                    .param("topic", "recursion")
                    .param("score", "25"));

            mockMvc.perform(post("/api/users/{userId}/mastery-records/progress", testUser.getId())
                            .param("topic", "recursion")
                            .param("score", "70"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.score", is(70)));

            mockMvc.perform(get("/api/users/{userId}/mastery-records", testUser.getId()))
                    .andExpect(jsonPath("$.length()", is(1)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/mastery-records/{id}")
    class DeleteMasteryRecordTests {

        @Test
        @DisplayName("Should delete mastery record successfully")
        void shouldDeleteMasteryRecord() throws Exception {
            MasteryRecordDto requestDto = MasteryRecordDto.builder()
                    .topic("loops")
                    .score(40)
                    .userId(testUser.getId())
                    .build();

            String response = mockMvc.perform(post("/api/users/{userId}/mastery-records", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            MasteryRecordDto created = objectMapper.readValue(response, MasteryRecordDto.class);

            mockMvc.perform(delete("/api/mastery-records/{id}", created.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/mastery-records/{id}", created.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existent mastery record ID")
        void shouldReturn404_WhenDeletingNonExistentMasteryRecord() throws Exception {
            mockMvc.perform(delete("/api/mastery-records/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }
}