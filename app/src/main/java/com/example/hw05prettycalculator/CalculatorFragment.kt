package com.example.hw05prettycalc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hw05prettycalculator.Computer
import com.example.hw05prettycalculator.databinding.FragmentCalculatorBinding
import kotlin.math.exp

class CalculatorFragment : Fragment() {
    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = requireNotNull(_binding)

    private var expression = BLANK_STRING
    private var result = BLANK_STRING
    private var numLeftBraces = 0
    private var numRightBraces = 0
    private val historyExpressions = mutableListOf<String>()
    private var operationCounter: Byte = 0 //it can't be more than 2 operations in a row
    private var stopDot = false //it is not acceptable to add dot
    private var memory = BLANK_STRING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        expression = savedInstanceState?.getString(EXPRESSION_KEY) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCalculatorBinding.inflate(layoutInflater, container, false)
            .also { _binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            showDisplayField(expression)
//            showResultField(BLANK_STRING)
            showMemoryField(BLANK_STRING)

            button0.setOnClickListener {
                onDigitClicked("0")
            }

            button1.setOnClickListener {
                onDigitClicked("1")
            }

            button2.setOnClickListener {
                onDigitClicked("2")
            }

            button3.setOnClickListener {
                onDigitClicked("3")
            }

            button4.setOnClickListener {
                onDigitClicked("4")
            }

            button5.setOnClickListener {
                onDigitClicked("5")
            }

            button6.setOnClickListener {
                onDigitClicked("6")
            }

            button7.setOnClickListener {
                onDigitClicked("7")
            }

            button8.setOnClickListener {
                onDigitClicked("8")
            }

            button9.setOnClickListener {
                onDigitClicked("9")
            }

            buttonDot.setOnClickListener {
                if (expression.isNotEmpty() &&
                    expression.takeLast(1) in DIGITS && !stopDot)
                {
                    appendElement(".")
                    stopDot = true //can not be more than 1 dot in number
                }
            }

            buttonDiv.setOnClickListener {
                onActionClicked("/")
            }

            buttonMult.setOnClickListener {
                onActionClicked("*")
            }

            buttonPlus.setOnClickListener {
                onActionClicked("+")
            }

            buttonMinus.setOnClickListener {
                onActionClicked("-")
            }

// left brace can place after LEFT_BRACE_ACCEPTABLE_SYM only and in empty string
            buttonBracketLeft.setOnClickListener {
                if (expression.isNotEmpty()) {
                    val lastSym = expression.takeLast(1)
                    if (lastSym in LEFT_BRACE_ACCEPTABLE_SYM) {
                        appendElement("(")
                        ++numLeftBraces
                    }
                } else {
                    appendElement("(")
                    ++numLeftBraces
                }
            }

// right brace can place after RIGHT_BRACE_ACCEPTABLE_SYM only and not in empty string
            buttonBracketRight.setOnClickListener {
                if (expression.isNotEmpty() &&
                    expression.takeLast(1) in RIGHT_BRACE_ACCEPTABLE_SYM &&
                    numRightBraces < numLeftBraces) {
                    appendElement(")")
                    ++numRightBraces
                    stopDot = false
                    operationCounter = 0
                }
            }

            buttonCancel.setOnClickListener {
                expression = BLANK_STRING
                numLeftBraces = 0
                numRightBraces = 0
                stopDot = false
                showDisplayField(BLANK_STRING)
//                showResultField(BLANK_STRING)
                operationCounter = 0
            }

            buttonDel.setOnClickListener {
                if (expression.isNotEmpty()) {
                    val last = expression.takeLast(1)
                    when (last) {
                        "(" -> {
                            --numLeftBraces
                            delElement()
                        }
                        ")" -> {
                            --numRightBraces
                            delElement()
                        }
                        DOT -> {
                            stopDot = false
                            delElement()
                        }
                        in OPERATIONS -> {
                            --operationCounter
                            delElement()
                        }
                        in DIGITS -> {
                            delElement()
                        }
                    }
                }
            }

            buttonEqual.setOnClickListener {
                result = getResult(expression)
//                showResultField(expression)
//                showDisplayField(expression.plus("=$result"))
                showDisplayField(expression + "=" + result)
            }

            buttonMemoryPlus.setOnClickListener {
                if (expression.isNotEmpty()) {
                    val res = getResult(expression)
                    if (res.isNotEmpty()) {
                        if (memory.isEmpty()) {
                            memory = res
                        } else {
                            memory = getResult(memory.plus(PLUS).plus(res))
                        }
                    }
//                    showResultField(res)
                    showMemoryField(memory)
                }
            }

            buttonMemoryMinus.setOnClickListener {
                if (expression.isNotEmpty()) {
                    val res = getResult(expression)
                    if (res.isNotEmpty()) {
                        if (memory.isEmpty()) {
                            memory = getResult(ZERO.plus(MINUS).plus(res))
                        } else {
                            memory = getResult(memory.plus(MINUS).plus(res))
                        }
                    }
//                    showResultField(res)
                    showMemoryField(memory)
                }
            }

            buttonMemoryRead.setOnClickListener {
                if (expression.isEmpty()) {
                    expression = memory
                } else {
                    when (expression.takeLast(1)) {
                        in "*/" -> expression += memory
                        PLUS -> {
                            if (memory.isNotEmpty() && memory.first().toString() == MINUS) {
                                expression = expression.dropLast(1).plus(memory)
                            } else {
                                expression += memory
                            }
                        }
                        MINUS -> {
                            if (memory.isNotEmpty() && memory.first().toString() == MINUS) {
                                //process unary minus
                                if (expression == MINUS || (expression.length >=2 &&
                                            expression.takeLast(2).dropLast(1) in "(*/")) {
                                    expression = expression.dropLast(1)
                                        .plus(memory.substring(1))
                                } else {
                                    //process binary minus
                                    expression = expression.dropLast(1).plus(PLUS)
                                        .plus(memory.substring(1))
                                }
                            } else {
                                expression += memory
                            }
                        }
                        in DIGITS.plus(BRACKETS) -> {
                            if (memory.isNotEmpty() && memory.first().toString() == MINUS) {
                                expression += memory
                            }
                        }
                    }
                }
                showDisplayField(expression)
//                showResultField(BLANK_STRING)
            }

            buttonMemoryClear.setOnClickListener {
                memory = BLANK_STRING
                showMemoryField(BLANK_STRING)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EXPRESSION_KEY, expression)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        expression = result
        _binding = null
    }

    private fun showDisplayField(showValue: String) {
        binding.displayField.text = showValue
    }

//    private fun showResultField(showValue: String) {
//        binding.resultField.text = showValue
//    }

    private fun showMemoryField(showValue: String) {
        binding.memoryField.text = showValue
    }

    private fun appendElement(element: String) {
        expression += element
        showDisplayField(expression)
    }

    private fun delElement() {
        expression = expression.dropLast(1)
        showDisplayField(expression)
//        showResultField(BLANK_STRING)
    }

    private fun onDigitClicked(digit: String) {

        if (expression.isNotEmpty()) {
            val last = expression.takeLast(1)
            if (last != ")") {
                when (last) {
                    in LEFT_BRACE_ACCEPTABLE_SYM -> {
                        appendElement(digit)
                        operationCounter = 0
                    }
                    in DIGITS -> {
                        if (last != ZERO) {
                            appendElement(digit)
                        } else if (expression.length >= 2) {
                            val previousLast = expression.takeLast(2).dropLast(1)
                            when (previousLast) {
                                in DIGITS -> appendElement(digit)
                                DOT -> appendElement(digit)
                                in LEFT_BRACE_ACCEPTABLE_SYM -> {
                                    expression = expression.dropLast(1)
                                    appendElement(digit)
                                }
                            }
                        } else {
                            expression = expression.dropLast(1)
                            appendElement(digit)
                        }
                    }
                    DOT -> {
                        appendElement(digit)
                    }
                }
            }
        } else {
            appendElement(digit)
            operationCounter = 0
        }
    }

    private fun onActionClicked(action: String) {
//        showResultField(BLANK_STRING)
        when (action) {
            MINUS -> {
                if (expression.isEmpty()) {
                    appendElement(MINUS)
                } else {
                    val last = expression.takeLast(1)
                    when (last) {
                        in (MINUS_ACCEPTABLE_SYM) -> appendElement(MINUS)
                        PLUS -> {
                            expression = expression.dropLast(1)
                            appendElement(MINUS)
                        }
                        MINUS -> showDisplayField(expression)
                    }
                }
            }
            in CHANGE_OPERATION_ACCEPTABLE_SYM -> {
                if (expression.isNotEmpty()) {
                    val last = expression.takeLast(1)
                    if (last in RIGHT_BRACE_ACCEPTABLE_SYM) {
                        appendElement(action)
                    } else if (last in OPERATIONS && expression.length >= 2) {
                        val previous = expression.takeLast(2).dropLast(1)
                        if (previous in RIGHT_BRACE_ACCEPTABLE_SYM) {
                            expression = expression.dropLast(1)
                            appendElement(action)
                        }
                    }
                }
            }
        }
        stopDot = false
    }

    private fun getResult(expression: String): String {
        val expr = expression
        var result = expr
        if (numLeftBraces == numRightBraces) {
            if (expr.isNotEmpty() && expr.takeLast(1) !in OPERATIONS.plus(DOT)) {
                result = Computer.compute(expr)
            }
        } else {
            Toast.makeText(requireContext(), "Unpaired brackets", Toast.LENGTH_SHORT)
                .show()
//            return expression
        }
        if (result.contains(DOT)) {//removal of meaningless zeros at the end of the fraction
            while (result.takeLast(1) == ZERO) {
                result = result.dropLast(1)
            }
        }
        return result
    }

    companion object {
        private const val LEFT_BRACE_ACCEPTABLE_SYM = "(/*-+"
        private const val RIGHT_BRACE_ACCEPTABLE_SYM = "0123456789)"
        private const val MINUS_ACCEPTABLE_SYM = "0123456789()*/"
        private const val CHANGE_OPERATION_ACCEPTABLE_SYM = "*/+"
        private const val OPERATIONS = "+-/*"
        private const val MINUS = "-"
        private const val PLUS = "+"
        private const val DIGITS = "0123456789"
        private const val BRACKETS = "()"
        private const val DOT = "."
        private const val ZERO = "0"
        private const val BLANK_STRING = ""
        private const val EXPRESSION_KEY = "expression_key"
    }
}