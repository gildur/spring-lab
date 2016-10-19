package pl.epoint.spring.lab

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

/**
 * @author Piotr Wolny
 */
@SpringBootTest
@AutoConfigureMockMvc
class Exercise07Specification extends Specification {

    def "should return all jobs on GET /job"() {
    }

    def "should return job data on GET /job/{id}"() {
    }

    def "should return all candidates for given job on GET /job/{id}/candidate"() {
    }

    def "should remove job on DELETE /job/{id}"() {
    }

    def "should add job on POST /job/{id}"() {
    }
}
