package com.example.domain

/**
 * Lightweight safe arithmetic expression evaluator for calculator input.
 * Supports +, -, *, /, parentheses, and decimals.
 */
object ExpressionEvaluator {

  fun evaluate(expression: String): Double? {
    val clean = expression.replace("×", "*").replace("÷", "/").replace("−", "-").trim()
    if (clean.isEmpty()) return null
    return try {
      parseExpression(clean)
    } catch (_: Exception) {
      null
    }
  }

  private fun parseExpression(expr: String): Double {
    var pos = -1
    var ch = -1

    fun nextChar() {
      ch = if (++pos < expr.length) expr[pos].code else -1
    }

    fun eat(charToEat: Int): Boolean {
      while (ch == ' '.code) nextChar()
      if (ch == charToEat) {
        nextChar()
        return true
      }
      return false
    }

    fun parseFactor(): Double {
      if (eat('+'.code)) return +parseFactor()
      if (eat('-'.code)) return -parseFactor()

      var x: Double
      val startPos = pos

      if (eat('('.code)) {
        x = parseExpression(expr)
        eat(')'.code)
      } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
        while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
        x = expr.substring(startPos, pos).toDouble()
      } else {
        throw RuntimeException("Unexpected char: " + ch.toChar())
      }

      return x
    }

    fun parseTerm(): Double {
      var x = parseFactor()
      while (true) {
        when {
          eat('*'.code) -> x *= parseFactor()
          eat('/'.code) -> {
            val divisor = parseFactor()
            x = if (divisor != 0.0) x / divisor else 0.0
          }
          else -> return x
        }
      }
    }

    nextChar()
    var x = parseTerm()
    while (true) {
      when {
        eat('+'.code) -> x += parseTerm()
        eat('-'.code) -> x -= parseTerm()
        else -> return x
      }
    }
  }
}
