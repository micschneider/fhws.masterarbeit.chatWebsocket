package fhws.masterarbeit.chatWebsocket.data;

public abstract class BasicMessage extends ChatMessage 
{
	protected String dataString;
	
	protected BasicMessage(String type, String dataString) 
	{
		super(type);
		this.dataString = dataString;
	}
	
	protected String getData()
	{
		return this.dataString;
	}
}
