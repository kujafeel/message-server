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

                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText() // "receiverId:message"
                            val (receiverId, message) = text.split(":", limit = 2)

                            // DB 저장
                            saveMessageToDB(senderId = userId, receiverId = receiverId, content = message)

                            // 받는 사람에게 WebSocket으로 전송
                            connections[receiverId]?.send("[$userId] $message")
                        }
                    }
                } finally {
                    connections.remove(userId)
                }
            }
        }
    }.start(wait = true)
}

fun saveMessageToDB(senderId: String, receiverId: String, content: String) {    
    val connection = DriverManager.getConnection(
        "jdbc:sqlserver://kujafeel.cafe24.com;databaseName=kujafeel;user=kujafeel;password=jygoo4691;"
    )


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

    connection.close()
}
