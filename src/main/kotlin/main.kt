import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import java.sql.DriverManager
import java.util.concurrent.ConcurrentHashMap

val connections = ConcurrentHashMap<String, WebSocketSession>()

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
        }

        routing {
            webSocket("/ws/{userId}") {
                val userId = call.parameters["userId"] ?: return@webSocket close()
                connections[userId] = this
                println("🟢 연결됨: $userId")
            
                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
            
                            val parts = text.split(":", limit = 2)
                            if (parts.size < 2) {
                                send("❗ 잘못된 메시지 형식입니다. '상대ID:메시지'")
                                continue
                            }
            
                            val (receiverId, message) = parts
            
                            saveMessageToDB(senderId = userId, receiverId = receiverId, content = message)
                            connections[receiverId]?.send("[$userId] $message")
                        }
                    }
                } catch (e: Exception) {
                    println("❗ WebSocket 에러 (${userId}): ${e.message}")
                } finally {
                    connections.remove(userId)
                    println("🟡 연결 종료: $userId")
                }
            }

        }
    }.start(wait = true)
}

fun saveMessageToDB(senderId: String, receiverId: String, content: String) {
    try {
        DriverManager.getConnection(
            "jdbc:sqlserver://kujafeel.cafe24.com;databaseName=kujafeel;user=kujafeel;password=jygoo4691;"
        ).use { connection ->
            val query = """
                INSERT INTO Messages (SenderId, ReceiverId, Content)
                VALUES (?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(query).use { stmt ->
                stmt.setString(1, senderId)
                stmt.setString(2, receiverId)
                stmt.setString(3, content)
                stmt.executeUpdate()
            }
        }
    } catch (e: Exception) {
        println("❗ DB 저장 중 오류 발생: ${e.message}")
    }
}

