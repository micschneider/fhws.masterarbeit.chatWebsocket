package fhws.masterarbeit.chatWebsocket.data;

public class ChatUpdateMessage extends StructuredMessage
{

	public ChatUpdateMessage(String username, String message) 
	{
		super(ChatMessage.CHAT_DATA_MESSAGE);
		super.dataList.add(username);
		super.dataList.add(message);
	}
	
	public String getUserName()
	{
		return (String) super.getList().get(0);
	}
	
	public String getMessage()
	{
		return (String) super.getList().get(1);
	}

}
