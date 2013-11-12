package fhws.masterarbeit.chatWebsocket.data;

public class UserSignoffMessage extends BasicMessage
{

	protected UserSignoffMessage(String username) 
	{
		super(ChatMessage.SIGNOFF_REQUEST, username);
	}
	
	public String getUsername()
	{
		return super.getData();
	}

}
