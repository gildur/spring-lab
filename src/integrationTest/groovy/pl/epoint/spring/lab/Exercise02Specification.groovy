package pl.epoint.spring.lab

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification

import java.time.LocalDate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author Piotr Wolny
 */
@SpringBootTest
@AutoConfigureMockMvc
class Exercise02Specification extends Specification {
    @Autowired
    MockMvc mvc

    def "should return OK status on GET /date"() {
        when:
        ResultActions result = mvc.perform(get("/date"))

        then:
        result.andExpect(status().isOk())
    }

    def "should return text/plain on GET /date"() {
        when:
        ResultActions result = mvc.perform(get("/date"))

        then:
        result.andExpect(content().contentType(MediaType.TEXT_PLAIN))
    }

    def "should return ISO date string on GET /date"() {
        when:
        ResultActions result = mvc.perform(get("/date"))

        then:
        result.andExpect(content().string(LocalDate.now().toString()))
    }

    def "should return METHOD NOT ALLOWED status on DELETE /date"() {
        when:
        ResultActions result = mvc.perform(delete("/date"))

        then:
        result.andExpect(status().isMethodNotAllowed())
    }
}
