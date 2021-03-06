package com.icosillion.pine

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.icosillion.pine.annotations.Use
import com.icosillion.pine.annotations.Group
import com.icosillion.pine.annotations.Route
import com.icosillion.pine.http.Method
import com.icosillion.pine.responses.JsonProblemResponse
import com.icosillion.pine.http.Request
import com.icosillion.pine.http.Response
import com.icosillion.pine.middleware.HtmlMiddleware
import com.icosillion.pine.middleware.JsonMiddleware
import com.icosillion.pine.responses.ValidationFailureResponse
import com.icosillion.pine.responses.modifiers.withText
import com.icosillion.pine.validator.Rule
import com.icosillion.pine.validator.objectSchema

class RootResource {

    @Route("/")
    fun root(request: Request, response: Response) {
        response.merge(JsonProblemResponse(405, "Method not implemented for this Route"))
    }
}

@Group("/test")
class TestResource {

    val personSchema = objectSchema(
            "name" to Rule().string(),
            "age" to Rule().integer()
    )

    @Route("/")
    fun root(request: Request, response: Response) {
        response.merge(JsonProblemResponse(405, "Method not implemented for this Route"))
    }

    @Use(HtmlMiddleware::class)
    @Route("/html")
    fun html(request: Request, response: Response) {
        response.body = "<!doctype html><html><head><title>Test</title></head><body><h1>Test</h1></body></html>"
    }

    @Use(JsonMiddleware::class)
    @Route("/json")
    fun json(request: Request, response: Response) {
        response.body = jsonObject(
                "test" to "test"
        )
    }

    @Use(JsonMiddleware::class)
    @Route("/validate", methods = arrayOf(Method.POST))
    fun schemaValidate(request: Request, response: Response) {
        val validationResult = personSchema.validateWithReporting(request.body as JsonObject)
        if(validationResult.isValid.not()) {
            response.merge(ValidationFailureResponse(validationResult))
            return
        }

        response.body = jsonObject(
                "status" to 200,
                "detail" to "Schema Valid!"
        )
    }
}

fun main(args: Array<String>) {
    val pine = Pine()

    pine.resource(RootResource())
    pine.resource(TestResource())
    pine.function(Method.GET, "/function", fun (request, response): Response {
        return response.withText("This was generated by a function resource")
    })

    pine.start()
}