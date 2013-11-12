package fhws.masterarbeit.chatWebsocket.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import fhws.masterarbeit.chatWebsocket.data.ChatDecoder;
import fhws.masterarbeit.chatWebsocket.data.ChatEncoder;
import fhws.masterarbeit.chatWebsocket.data.ChatMessage;
import fhws.masterarbeit.chatWebsocket.data.ChatUpdateMessage;
import fhws.masterarbeit.chatWebsocket.data.NewUserMessage;
import fhws.masterarbeit.chatWebsocket.data.UserListUpdateMessage;
import fhws.masterarbeit.chatWebsocket.data.UserSignoffMessage;

@ServerEndpoint(value = "/chatWebsocket",
				subprotocols = "chat",
				encoders = {ChatEncoder.class},
				decoders = {ChatDecoder.class},
				configurator = ChatServerConfigurator.class)
public class ChatServer 
{
	public static String USERNAME_KEY = "username";
	public static String USERNAMES_KEY = "usernames";
	private Session session;
	private ServerEndpointConfig sepc; 
	private Transcript transcript;
	
	@OnOpen
	public void startChatChannel(EndpointConfig epc, Session session)
	{
		this.session = session;
		this.sepc = (ServerEndpointConfig) epc;
		ChatServerConfigurator csc = (ChatServerConfigurator) sepc.getConfigurator();
		this.transcript = csc.getTranscript();
	}
	
	@OnMessage
	public void handleChatMessage(ChatMessage message)
	{
		switch (message.getType())
		{
			case ChatMessage.USERNAME_MESSAGE:
				this.processNewUser((NewUserMessage) message);
			case ChatMessage.CHAT_DATA_MESSAGE:
				this.processChatUpdate((ChatUpdateMessage) message);
			case ChatMessage.SIGNOFF_REQUEST:
				this.processSignoffRequest((UserSignoffMessage)message);
		}
	}
	
	@OnError
	public void handleError(Throwable t)
	{
		System.out.println("Error: " + t.getMessage());
	}
	
	@OnClose
	public void endChatChannel()
	{
		if(this.getCurrentUsername() != null)
		{
			this.addMessage("just left...without even signing out!");
			this.removeUser();
		}
	}

	private void removeUser() 
	{
		try
		{
			this.updateUserList();
			this.broadcastUserListUpdate();
			this.session.getUserProperties().remove(USERNAME_KEY);
			this.session.close(new CloseReason(
				CloseReason.CloseCodes.NORMAL_CLOSURE, "User logged off"));
		}
		catch(IOException e)
		{
			System.out.println("Error removing user");
		}
	}

	private void addMessage(String message) 
	{
		this.transcript.addEntry(this.getCurrentUsername(), message);
		this.broadcastTranscriptUpdate();
	}
	
	private void broadcastTranscriptUpdate()
	{
		for(Session nextSession : session.getOpenSessions())
		{
			ChatUpdateMessage cupm = new ChatUpdateMessage(this.transcript.getLastUsername(), 
															this.transcript.getLastMessage());
			try
			{
				nextSession.getBasicRemote().sendObject(cupm);
			}
			catch(IOException | EncodeException e)
			{
				System.out.println("Error updating a client : " + e.getMessage());
			}
		}
	}
	
	private List<String> getUserList()
	{
		List<String> userList = (List<String>)this.sepc.getUserProperties().get(USERNAMES_KEY);
		return(userList == null) ? new ArrayList<String>() : userList;
	}

	private String getCurrentUsername() 
	{
		return (String) session.getUserProperties().get(USERNAME_KEY);
	}

	private void processSignoffRequest(UserSignoffMessage message) 
	{
		this.addMessage(" just left.");
		this.removeUser();
	}

	private void processChatUpdate(ChatUpdateMessage message) 
	{
		this.addMessage(message.getMessage());
	}

	private void processNewUser(NewUserMessage message) 
	{
		String newUsername = this.validateUsername(message.getUsername());
		NewUserMessage uMessage = new NewUserMessage(newUsername);
		try
		{
			session.getBasicRemote().sendObject(uMessage);
		}
		catch(IOException | EncodeException ioe)
		{
			System.out.println("Error singning " + message.getUsername() + " into chat: " + ioe.getMessage());
		}
		
		this.registerUser(newUsername);
		this.broadcastUserListUpdate();
		this.addMessage(" just joined.");
	}

	private void broadcastUserListUpdate() 
	{
		UserListUpdateMessage ulum = new UserListUpdateMessage(this.getUserList());
		for (Session nextSession : session.getOpenSessions())
		{
			try
			{
				nextSession.getBasicRemote().sendObject(ulum);
			}
			catch(IOException | EncodeException ioe)
			{
				System.out.println("Error updating a client : " + ioe.getMessage());
			}
		}
	}

	private void registerUser(String newUsername) 
	{
		session.getUserProperties().put(USERNAME_KEY, newUsername);
		this.updateUserList();
	}

	private void updateUserList() 
	{
		List<String> usernames = new ArrayList<>();
		for (Session s : session.getOpenSessions())
		{
			String uname = (String) s.getUserProperties().get(USERNAME_KEY);
			usernames.add(uname);
		}
		this.sepc.getUserProperties().put(USERNAMES_KEY, usernames);
	}

	private String validateUsername(String newUsername) 
	{
		if (this.getUserList().contains(newUsername))
			return this.validateUsername(newUsername + "1");
		return newUsername;
	}
}
