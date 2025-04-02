package ru.yarsu

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.*
import org.http4k.lens.contentType
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.domain.MovementType
import ru.yarsu.domain.ProductMovementStore
import ru.yarsu.domain.ProductStore

fun main() {
    val productMovementStore = ProductMovementStore.fromCsv()
    val productStore = ProductStore.fromCsv()

    val list: MutableList<ProductJson> = mutableListOf()
    productStore.list().forEach{ list.add(ProductJson(it.name, 0.0)) }


    val productMovementStoreList = productMovementStore.list()

    //Проходимся по всем действиям в ProductMovementStore и прибавляем/уменьшаем значения ConsumedAmount для каждого объекта в списке list
    productMovementStoreList.forEach{
        val productId = it.productId
        var productName: String = ""
        for (i in productStore.list()){
            if (productId == i.id){
                productName = i.name
                break
            }
        }

        //Add
        if (it.type == MovementType.ADDITION){
            for (i in list){
                if (productName == i.ProductName){
                    i.ConsumedAmount += it.amount
                    break
                }
            }
        }
        //Remove
        else{
            for (i in list){
                if (productName == i.ProductName){
                    i.ConsumedAmount -= it.amount
                    break
                }
            }
        }
    }

    list.sortByDescending { it.ConsumedAmount }

    val getHandler: HttpHandler = {request: Request ->

        val count1 = request.query("top")
        if (count1 != null){
            val count2 = count1.toIntOrNull()
            if (count2 != null){
                if (count2 < 0){
                    val jsonString = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ErrorMessage("Параметр не должен быть отрицательным"))
                    Response(Status.BAD_REQUEST).body(jsonString).contentType(ContentType.APPLICATION_JSON)
                }
                else{
                    val filteredList: MutableList<ProductJson> = mutableListOf()
                    for (i in 0 until count2) {
                        filteredList.add(list[i])
                    }
                    val jsonString = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(filteredList)
                    Response(Status.OK).body(jsonString).contentType(ContentType.APPLICATION_JSON)
                }
            }
            else{
                val jsonString = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ErrorMessage("Параметр должен быть положительным числом"))
                Response(Status.BAD_REQUEST).body(jsonString).contentType(ContentType.APPLICATION_JSON)
            }
        }
        else{
            val filteredList: MutableList<ProductJson> = mutableListOf()
            for (i in 0..4) {
                filteredList.add(list[i])
            }
            val jsonString = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(filteredList)
            Response(Status.OK).body(jsonString).contentType(ContentType.APPLICATION_JSON)
        }
    }

    val postHandler: HttpHandler = { request: Request ->
        val data = request.bodyString()
        println(data)
        try {
            val postJsonData1 = ObjectMapper().readValue<postJsonData>(data)
            val name = postJsonData1.name
            val type = postJsonData1.type
            val amount = postJsonData1.amount
            if (type == 0) {
                var flag = 0
                for (i in list) {
                    if (i.ProductName == name) {
                        i.ConsumedAmount += amount
                        flag = 1
                    }
                }
                if (flag == 0){
                    list.add(ProductJson(name, amount))
                }
            }
            if (type == 1) {
                for (i in list) {
                    if (i.ProductName == name) {
                        i.ConsumedAmount -= amount
                    }
                }
            }
            Response(Status.OK)
        }
        catch (e: Exception) {
            Response(Status.BAD_REQUEST)
        }
    }

    val app = routes(
        "/top-products" bind Method.GET to getHandler,
        "/add-product" bind Method.POST to postHandler
    )

    val server = app.asServer(Netty(9000)).start()


}

data class postJsonData(val name: String, val amount: Double, val type: Int)

data class ProductJson(val ProductName: String, var ConsumedAmount: Double)
data class ErrorMessage(val Error: String)

