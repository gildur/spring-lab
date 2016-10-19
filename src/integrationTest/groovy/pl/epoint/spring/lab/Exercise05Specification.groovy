package pl.epoint.spring.lab

import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import javax.persistence.EntityManager

import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * @author Piotr Wolny
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Exercise05Specification extends Specification {
    @Autowired
    MockMvc mvc
    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    EntityManager entityManager

    def "should return all candidates on GET /candidate"() {
        given:
        RequestBuilder getRequest = get("/candidate")

        when:
        ResultActions result = mvc.perform(getRequest)

        then:
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$').value(hasSize(countRowsInTable(jdbcTemplate, "candidate"))))
                .andExpect(jsonPath('$..id').exists())
                .andExpect(jsonPath('$..firstName').exists())
                .andExpect(jsonPath('$..lastName').exists())
                .andExpect(jsonPath('$..email').exists())
                .andExpect(jsonPath('$..birthYear').exists())
    }

    def "should add candidate on POST /candidate"() {
        given:
        int candidateRowCount = countRowsInTable(jdbcTemplate, "candidate")
        RequestBuilder postRequest = post("/candidate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
    "firstName": "Mariusz",
    "lastName": "Nowy",
    "email": "nowy@astalavista.co.uk",
    "birthYear": 1996
}""")

        when:
        ResultActions result = mvc.perform(postRequest)

        then:
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').isNumber())
                .andExpect(jsonPath('$.firstName').value("Mariusz"))
                .andExpect(jsonPath('$.lastName').value("Nowy"))
                .andExpect(jsonPath('$.email').value("nowy@astalavista.co.uk"))
                .andExpect(jsonPath('$.birthYear').value(1996))
        candidateRowCount + 1 == countRowsInTable(jdbcTemplate, "candidate")

        String response = result.andReturn().response.contentAsString
        Integer id = JsonPath.read(response, '$.id')
        Map<String, Object> dbRow = jdbcTemplate.queryForMap("select * from candidate where id = ?", id)
        dbRow.get("ID") == id
        dbRow.get("FIRST_NAME") == "Mariusz"
        dbRow.get("LAST_NAME") == "Nowy"
        dbRow.get("EMAIL") == "nowy@astalavista.co.uk"
        dbRow.get("BIRTH_YEAR") == 1996
    }

    def "should return NOT FOUND status on DELETE /candidate/{id} for unknown id"() {
        given:
        RequestBuilder deleteRequest = delete("/candidate/1001")

        when:
        ResultActions result = mvc.perform(deleteRequest)

        then:
        result.andExpect(status().isNotFound())
    }

    def "should delete candidate on DELETE /candidate/{id}"() {
        given:
        int candidateRowCount = countRowsInTable(jdbcTemplate, "candidate")
        RequestBuilder deleteRequest = delete("/candidate/1")

        when:
        ResultActions result = mvc.perform(deleteRequest)
        entityManager.flush()

        then: "should return removed data"
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').value("1"))
                .andExpect(jsonPath('$.firstName').value("Jan"))
                .andExpect(jsonPath('$.lastName').value("Kowalski"))
                .andExpect(jsonPath('$.email').value("jan@kowalski.com"))
                .andExpect(jsonPath('$.birthYear').value(1976))
        candidateRowCount - 1 == countRowsInTable(jdbcTemplate, "candidate")
    }

    def "should return NOT FOUND status on GET /candidate/{id} for unknown id"() {
        given:
        RequestBuilder getRequest = get("/candidate/1001")

        when:
        ResultActions result = mvc.perform(getRequest)

        then:
        result.andExpect(status().isNotFound())
    }

    def "should retrieve candidate data on GET /candidate/{id}"() {
        given:
        RequestBuilder getRequest = get("/candidate/1")

        when:
        ResultActions result = mvc.perform(getRequest)

        then:
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').value("1"))
                .andExpect(jsonPath('$.firstName').value("Jan"))
                .andExpect(jsonPath('$.lastName').value("Kowalski"))
                .andExpect(jsonPath('$.email').value("jan@kowalski.com"))
                .andExpect(jsonPath('$.birthYear').value(1976))
    }

    def "should return NOT FOUND status on PUT /candidate/{id} for unknown id"() {
        given:
        RequestBuilder putRequest = get("/candidate/1001")

        when:
        ResultActions result = mvc.perform(putRequest)

        then:
        result.andExpect(status().isNotFound())
    }

    def "should update candidate data on PUT /candidate/{id}"() {
        given:
        RequestBuilder putRequest = put("/candidate/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
    "firstName": "Jan",
    "lastName": "Robak",
    "email": "robak@gmail.com",
    "birthYear": 1976
}""")

        when:
        ResultActions result = mvc.perform(putRequest)
        entityManager.flush()

        then: "should return old data"
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').value("1"))
                .andExpect(jsonPath('$.firstName').value("Jan"))
                .andExpect(jsonPath('$.lastName').value("Kowalski"))
                .andExpect(jsonPath('$.email').value("jan@kowalski.com"))
                .andExpect(jsonPath('$.birthYear').value(1976))

        Map<String, Object> dbRow = jdbcTemplate.queryForMap("select * from candidate where id = 1")
        dbRow.get("ID") == 1
        dbRow.get("FIRST_NAME") == "Jan"
        dbRow.get("LAST_NAME") == "Robak"
        dbRow.get("EMAIL") == "robak@gmail.com"
        dbRow.get("BIRTH_YEAR") == 1976
    }
}
