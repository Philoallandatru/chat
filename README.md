# Brief Introduction
	This program implements a Chat Room on LAN in Java
---- 
# Analysis of Functionalities
- Multiple Clients Chat
	The Server waits for connection in a loop, each time a client find him, the server starts a thread to handle the connection and the following communication.
- History Review
	The Client writes all the message it sends or it receives(from the server) into a txt file, and when the user wanna review the chat history, it reads all the content and display it in a pane.
---- 
# Modules
## Client Module
- ChatClient Class
	- ChatClient()
		Initialize the Client GUI window, add listener to menu items
	- run()
		Read message from server in an infinite loop
	- connect()
		Create a socket, get Input and Output stream from this socket, and create a new thread to receive messages.
## Server Module
If rewrite with MVC
### - Model

### - View

### - Constroller

---- 

# Flowchart of Functionality
![][image-1]

---- 
# System Environment
- macOS 13.14
- CentOS 7  
- Ubuntu 16.04
---- 
# Development Tools
- Git 
- IDEA
- Github
---- 
# Instruction
	Start the server program on a machine, then start some clients on machines which are connected through LAN, fill the name, the IP address and the port number of the server process, click “Connect”. As the connection is established, you can start to chat.

[image-1]:	Assert/flowchart.png