package ru.yarsu

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.http4k.core.*
import org.http4k.lens.contentType
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.domain.MovementType
import ru.yarsu.domain.Product
import ru.yarsu.domain.ProductMovement
import ru.yarsu.domain.ProductMovementStore
import ru.yarsu.domain.ProductStore
import java.time.LocalDateTime
import java.util.ResourceBundle
import java.util.UUID
import kotlin.random.Random

fun main() {
    val productMovementStore = ProductMovementStore.fromCsv()
    val productStore = ProductStore.fromCsv()

    val productList: MutableList<Product1> = mutableListOf()

    val productStoreList = productStore.list()
    productStoreList.forEach { productList.add(Product1(it.name, 0.0))}

    for (it in productMovementStore.list()){
        var name: String? = null
        for (t in productStore.list()){
            if (it.id == t.id)
                name = t.name
        }
        if (it.type == MovementType.ADDITION){
            for( i in productList){
                if (i.ProductName == name){
                    i.ConsumedAmount += it.amount
                }
            }
        }
    }

    val getHandler: HttpHandler = { request: Request ->
        val parameter = request.query("product-count")
        val filterList: MutableList<Product1> = mutableListOf()
        if (parameter != null) {
            if (parameter.toInt() > 0) {
                for (i in 0 until parameter!!.toInt()) {
                    filterList.add(productList[i])
                    val jsonString = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(filterList)
                    Response(Status.OK).body(jsonString)
                }
            }
            else {
                Response(Status.BAD_REQUEST).body("{\n" +
                        "    \"Error\": \"Значение параметра product-count содержит отрицательное число.\"\n" +
                        "}").contentType(ContentType.APPLICATION_JSON)
            }
        }
        val jsonString = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(productList)
        Response(Status.OK).body(jsonString).contentType(ContentType.APPLICATION_JSON)

    }

    val app = routes(
        "/top-products" bind Method.GET to getHandler
    )

    val server = app.asServer(Netty(9000)).start()

    val pingRouteHandler: HttpHandler = { Response(Status.OK).body("pong") }
//    val server = pingRouteHandler.asServer(Netty(9000))
//    server.start()
    //println("Application is available on http://localhost:${server.port()}")


}

data class Product1(val ProductName: String, var ConsumedAmount: Double)
data class Error(val Error: String)

