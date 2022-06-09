package isel.leic.pc.roomchat

open class Line {
    private constructor() {}

    class Message(val value : String) : Line()

    class EnterRoomCommand (val name : String) : Line()

    class ShutdownCommand (val timeout : Long) : Line()

    class LeaveRoomCommand   : Line()

    class ExitCommand  : Line()

    class InvalidLine(val reason: String) : Line()

    companion object {

        fun parse(line : String) : Line {
            if (!line.startsWith("/"))
            {
                return Message(line);
            }

            var parts = line.split(" ");
            return when(parts[0]) {
                "/enter" -> ParseEnterRoom(parts)
                "/leave" -> ParseLeaveRoom(parts)
                "/exit" -> ParseExit(parts)
                "/shutdown" -> ParseShutdown(parts)
                else->  InvalidLine("Unknown command")
            }
        }

        private fun ParseEnterRoom(parts: List<String>): Line {
            return if (parts.size != 2) {
                InvalidLine("/enter command requires exactly one argument")
            } else EnterRoomCommand(parts[1])
        }

        private fun ParseLeaveRoom(parts: List<String>): Line {
            return if (parts.size != 1) {
                InvalidLine("/leave command does not have arguments")
            } else LeaveRoomCommand()
        }

        private fun ParseExit(parts: List<String>): Line {
            return if (parts.size != 1) {
                InvalidLine("/exit command does not have arguments")
            } else ExitCommand()
        }

        private fun ParseShutdown(parts: List<String>): Line {
            return if (parts.size != 2) {
                InvalidLine("/exit command does not have arguments")
            } else ShutdownCommand(parts[1].toLong())
        }
    }

}