package pl.epoint.spring.lab

import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * @author Piotr Wolny
 */
@SpringBootTest
@AutoConfigureMockMvc
class Exercise03Specification extends Specification {
    @Autowired
    MockMvc mvc

    def "should return OK status on GET /customer"() {
        when:
        ResultActions result = mvc.perform(get("/customer"))

        then:
        result.andExpect(status().isOk())
    }

    def "should return application/json on GET /customer"() {
        when:
        ResultActions result = mvc.perform(get("/customer"))

        then:
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }

    def "should return JSON array on GET /customer"() {
        when:
        ResultActions result = mvc.perform(get("/customer"))

        then:
        result.andExpect(jsonPath('$').isArray())
    }

    def "should return UNSUPPORTED MEDIA TYPE status on POST /customer without JSON"() {
        given:
        RequestBuilder requestBuilder = post("/customer")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Jan Kowalski")

        when:
        ResultActions result = mvc.perform(requestBuilder)

        then:
        result.andExpect(status().isUnsupportedMediaType())
    }

    def "should return BAD REQUEST status on POST /customer with invalid JSON"() {
        given:
        RequestBuilder requestBuilder = post("/customer")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("""{
    "firstName" "Jan",
    "lastName" "Kowalski"
}""")

        when:
        ResultActions result = mvc.perform(requestBuilder)

        then:
        result.andExpect(status().isBadRequest())
    }

    def "should return CREATED status on POST /customer"() {
        given:
        RequestBuilder requestBuilder = post("/customer")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("""{
    "firstName": "Jan",
    "lastName": "Kowalski"
}""")

        when:
        ResultActions result = mvc.perform(requestBuilder)

        then:
        result.andExpect(status().isCreated())
    }

    def "should return application/json on POST /customer"() {
        given:
        RequestBuilder requestBuilder = post("/customer")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("""{
    "firstName": "Jan",
    "lastName": "Kowalski"
}""")

        when:
        ResultActions result = mvc.perform(requestBuilder)

        then:
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }

    def "should generate identifier on POST /customer"() {
        given:
        RequestBuilder requestBuilder = post("/customer")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("""{
    "firstName": "Jan",
    "lastName": "Kowalski"
}""")

        when:
        ResultActions result = mvc.perform(requestBuilder)

        then:
        result.andExpect(jsonPath('$.id').isNumber())
              .andExpect(jsonPath('$.firstName').value("Jan"))
              .andExpect(jsonPath('$.lastName').value("Kowalski"))
    }

    def "GET /customer should return one more customer after add"() {
        given:
        RequestBuilder getAllRequest = get("/customer")

        when:
        ResultActions getAllResult = mvc.perform(getAllRequest)

        then: "customer list is returned"
        getAllResult.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$').isArray())
        Integer count = JsonPath.read(getAllResult.andReturn().response.contentAsString, '$').size()

        RequestBuilder addRequest = post("/customer")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("""{
    "firstName": "Jan",
    "lastName": "Kowalski"
}""")
        when:
        ResultActions addResult = mvc.perform(addRequest)

        then: "customer is added and returned with generated id"
        addResult.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').isNumber())

        when:
        ResultActions secondGetAllResult = mvc.perform(getAllRequest)

        then: "customer list is returned with one more customer"
        secondGetAllResult.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$').isArray())
        count + 1 == JsonPath.read(secondGetAllResult.andReturn().response.contentAsString, '$').size()
    }

    def "should be possible to add customer and then retrieve its data"() {
        given:
        RequestBuilder addRequest = post("/customer")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("""{
    "firstName": "Jan",
    "lastName": "Kowalski"
}""")
        when:
        ResultActions addResult = mvc.perform(addRequest)

        then: "customer is added and returned with generated id"
        addResult.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').isNumber())

        String addResponse = addResult.andReturn().response.contentAsString
        Integer id = JsonPath.read(addResponse, '$.id')
        RequestBuilder getRequest = get("/customer/{id}", id)

        when:
        ResultActions getResult = mvc.perform(getRequest)

        then: "customer is found and returned"
        getResult.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').value(id))
                .andExpect(jsonPath('$.firstName').value("Jan"))
                .andExpect(jsonPath('$.lastName').value("Kowalski"))
    }

    def "should be possible to add customer, remove it and then not possible to retrieve its data"() {
        given:
        RequestBuilder addRequest = post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
    "firstName": "Jan",
    "lastName": "Kowalski"
}""")

        when:
        ResultActions addResult = mvc.perform(addRequest)

        then: "customer is added and returned with generated id"
        addResult.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').isNumber())

        String addResponse = addResult.andReturn().response.contentAsString
        Integer id = JsonPath.read(addResponse, '$.id')
        RequestBuilder deleteRequest = delete("/customer/{id}", id)

        when:
        ResultActions deleteResult = mvc.perform(deleteRequest)

        then: "customer is removed and returned"
        deleteResult.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').value(id))
                .andExpect(jsonPath('$.firstName').value("Jan"))
                .andExpect(jsonPath('$.lastName').value("Kowalski"))

        RequestBuilder getRequest = get("/customer/{id}", id)

        when:
        ResultActions getResult = mvc.perform(getRequest)

        then: "customer is not found"
        getResult.andExpect(status().isNotFound())
    }

    def "should be possible to add customer, update it and then retrieve its updated data"() {
        given:
        RequestBuilder addRequest = post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
    "firstName": "Jan",
    "lastName": "Kowalski"
}""")

        when:
        ResultActions addResult = mvc.perform(addRequest)

        then: "customer is added and returned with generated id"
        addResult.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').isNumber())

        String addResponse = addResult.andReturn().response.contentAsString
        Integer id = JsonPath.read(addResponse, '$.id')
        RequestBuilder putRequest = put("/customer/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
    "id": ${id},
    "firstName": "Marian",
    "lastName": "Nowak"
}""")

        when:
        ResultActions deleteResult = mvc.perform(putRequest)

        then: "customer is updated and returned"
        deleteResult.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').value(id))
                .andExpect(jsonPath('$.firstName').value("Marian"))
                .andExpect(jsonPath('$.lastName').value("Nowak"))

        RequestBuilder getRequest = get("/customer/{id}", id)

        when:
        ResultActions getResult = mvc.perform(getRequest)

        then: "customer is found and returned"
        getResult.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.id').value(id))
                .andExpect(jsonPath('$.firstName').value("Marian"))
                .andExpect(jsonPath('$.lastName').value("Nowak"))
    }
}
