package com.imfinix.look

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AiApi {

    data class Reply(val ok: Boolean, val text: String)

    private const val SYSTEM_PROMPT = "Voce e o assistente do ImfinixLook, app de personalizacao do Infinix Hot 50i (XOS, Android). Responda em portugues brasileiro, de forma curta, direta e amigavel. Ajude com personalizacao do celular, dicas de Android/XOS e duvidas gerais."

    private fun post(url: String, headers: Map<String, String>, body: JSONObject): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 60000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it, "UTF-8")).readText() } ?: ""
            Pair(code, text)
        } catch (e: Exception) {
            Pair(-1, e.message ?: "erro de rede")
        } finally {
            conn.disconnect()
        }
    }

    private fun friendlyError(code: Int, raw: String, provedor: String): String {
        val detail = try {
            val j = JSONObject(raw)
            val err = j.optJSONObject("error")
            err?.optString("message")?.takeIf { it.isNotEmpty() } ?: j.optString("message")
        } catch (e: Exception) { "" }
        return when (code) {
            -1 -> "Sem internet ou o serviço não respondeu. Confere a conexão e tenta de novo."
            401, 403 -> "Chave inválida ou sem permissão. Confere a chave do $provedor nas configurações."
            429 -> "Limite de uso atingido na sua conta do $provedor. Espera um pouco ou confere seus créditos."
            404 -> "Modelo não encontrado no $provedor. Confere o nome do modelo nas configurações."
            else -> "O $provedor respondeu erro $code.${if (detail.isNotEmpty()) " Detalhe: $detail" else ""}"
        }
    }

    fun claude(key: String, model: String, history: List<Pair<String, String>>): Reply {
        val msgs = JSONArray()
        history.forEach { (role, text) ->
            msgs.put(JSONObject().put("role", role).put("content", text))
        }
        val body = JSONObject()
            .put("model", model.ifEmpty { "claude-sonnet-5-5" })
            .put("max_tokens", 1024)
            .put("system", SYSTEM_PROMPT)
            .put("messages", msgs)
        val (code, raw) = post(
            "https://api.anthropic.com/v1/messages",
            mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01"),
            body
        )
        if (code !in 200..299) return Reply(false, friendlyError(code, raw, "Claude"))
        return try {
            val j = JSONObject(raw)
            val content = j.getJSONArray("content")
            val sb = StringBuilder()
            for (i in 0 until content.length()) {
                val c = content.getJSONObject(i)
                if (c.optString("type") == "text") sb.append(c.optString("text"))
            }
            Reply(true, sb.toString().ifEmpty { "(resposta vazia)" })
        } catch (e: Exception) {
            Reply(false, "Não consegui ler a resposta do Claude.")
        }
    }

    fun perplexity(key: String, model: String, history: List<Pair<String, String>>): Reply {
        val input = JSONArray()
        history.forEach { (role, text) ->
            input.put(JSONObject().put("type", "message").put("role", role).put("content", text))
        }
        val body = JSONObject()
            .put("model", model.ifEmpty { "sonar" })
            .put("input", input)
            .put("instructions", SYSTEM_PROMPT)
            .put("tools", JSONArray().put(JSONObject().put("type", "web_search")))
        val (code, raw) = post(
            "https://api.perplexity.ai/v1/agent",
            mapOf("Authorization" to "Bearer $key"),
            body
        )
        if (code !in 200..299) return Reply(false, friendlyError(code, raw, "Perplexity"))
        return try {
            val j = JSONObject(raw)
            val out = j.getJSONArray("output")
            val sb = StringBuilder()
            for (i in 0 until out.length()) {
                val item = out.getJSONObject(i)
                if (item.optString("type") == "message") {
                    val content = item.optJSONArray("content") ?: continue
                    for (k in 0 until content.length()) {
                        val c = content.getJSONObject(k)
                        if (c.optString("type") == "output_text") sb.append(c.optString("text"))
                    }
                }
            }
            Reply(true, sb.toString().ifEmpty { "(resposta vazia)" })
        } catch (e: Exception) {
            Reply(false, "Não consegui ler a resposta do Perplexity.")
        }
    }
}
