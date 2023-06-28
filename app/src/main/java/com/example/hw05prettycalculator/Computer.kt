package com.example.hw05prettycalculator

import java.math.BigDecimal
import java.math.RoundingMode

object Computer {

    private fun findAndReplaceUnaryMinus(expression: String): String {

        return expression.replace(CONVERT_UNARY_MINUS_TO_TILDA.toRegex(), "~")
    }

    //converts string to List of Reverse Polish Notation
    private  fun convertStringToRPNinList(expression: String): List<String> {

        val rpn = mutableListOf<String>() //Reverse Polish Notation
        val stack = mutableListOf<String>() //Stack for operations
        var flagDigit = false //If number in string is detected
        var unaryMinusFlag = false //If unary minus before number or bracket
        var numberTempStore = "" //Temp store for digits

        //frequently repeated block 1 - push number to rpn from temporary store
        fun pushNumberFromNumberTempStoreToRPN() {

            if (flagDigit) {
                rpn.add(numberTempStore)
                numberTempStore = ""
                flagDigit = false
            }
        }

        //frequently repeated block 2 - push last operation to rpn from stack
        fun pushLastOperationFromStackToRPN () {

            rpn.add(stack.removeLast())
        }

        expression.forEach {
            when (it) {
                '~' -> unaryMinusFlag = true
                in "0123456789." -> {
                    if (unaryMinusFlag) {
                        numberTempStore += "-"
                        unaryMinusFlag = false
                    }
                    numberTempStore += it
                    flagDigit = true
                }
                in ACCEPTABLE_ACTION -> {
                    pushNumberFromNumberTempStoreToRPN()
                    //while операция на вершине стека приоритетнее, или такого же уровня приоритета как o1
                    //… выталкиваем верхний элемент стека в выходную строку; помещаем операцию o1 в стек
                    while (stack.isNotEmpty() && (stack.last() in "*/" || stack.last() in "+-" && it in "+-")) {
                        pushLastOperationFromStackToRPN()
                    }
                    stack.add(it.toString())
                }
                in "()" -> {
                    if (it == '(') {
                        if (unaryMinusFlag) {
                            stack.add("~")
                            unaryMinusFlag = false
                        }
                        stack.add(it.toString())
                    } else { // if it == ")"
                        pushNumberFromNumberTempStoreToRPN()
                        while (stack.last() != "(") {
                            pushLastOperationFromStackToRPN()
                        }
                        stack.removeLast() //removing "(" from the stack
                        if (stack.isNotEmpty() && stack.last() == "~") {
                            //if unary minus is before opening bracket, push it from the stack to rpn
                            pushLastOperationFromStackToRPN()
                        }
                    }
                }
            }
        }
        pushNumberFromNumberTempStoreToRPN() //adding number to output string if last

        while (stack.isNotEmpty()) {
            //when input string is over, push all the operations from the stack to output string
            pushLastOperationFromStackToRPN()
        }
        return rpn.toList()
    }

    fun compute(expr: String): String {

        val rpn = convertStringToRPNinList(findAndReplaceUnaryMinus(expr))

        val stack = emptyList<BigDecimal>().toMutableList() //Computer stack
        var operandRight: BigDecimal
        var operandLeft: BigDecimal
        var index = 0 //action's order
        rpn.forEach { token ->
            when (token) {
                "-" -> {
                    operandRight = stack.removeLast()
                    operandLeft = stack.removeLast()
                    stack.add(operandLeft.subtract(operandRight))
                    ++index
                }
                "+" -> {
                    operandRight = stack.removeLast()
                    operandLeft = stack.removeLast()
                    stack.add(operandLeft.add(operandRight))
                    ++index
                }
                "*" -> {
                    operandRight = stack.removeLast()
                    operandLeft = stack.removeLast()
                    stack.add(operandLeft.multiply(operandRight).setScale(6, RoundingMode.HALF_UP))
                    ++index
                }
                "/" -> {
                    operandRight = stack.removeLast()
                    if (operandRight.compareTo(BigDecimal.ZERO) == 0) {
                        return "Divide by zero"
                    }
                    operandLeft = stack.removeLast()
                    stack.add(operandLeft.divide(operandRight, 6, RoundingMode.HALF_UP))
                    ++index
                }
                "~" -> {
                    operandRight = BigDecimal.ZERO.subtract(stack.removeLast())
                    stack.add(operandRight)
                    ++index
                }
                else -> { //if token is number
                    stack.add(token.toBigDecimal())
                }
            }
        }
        val result = stack.removeLast()
        //checking for existing of a fractional part. If no convert to BigInteger
        if (result.subtract(result.toBigInteger().toBigDecimal()).compareTo(BigDecimal.ZERO) == 0) {
            return result.toBigInteger().toString()
        }
        return result.toPlainString()

    }

    //converting unary minus to ~ (if before "-" not number or closing bracket)
    //Унарный минус — это оператор - (минус), перед которым в арифметическом выражении
    // всегда стоит не число, и не закрывающая скобка
    private const val CONVERT_UNARY_MINUS_TO_TILDA = """(?<!(\d)|\))-"""
    private const val ACCEPTABLE_ACTION = "+-/*"
}
