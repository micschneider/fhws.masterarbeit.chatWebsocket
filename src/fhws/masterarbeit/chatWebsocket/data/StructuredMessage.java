package fhws.masterarbeit.chatWebsocket.data;

import java.util.ArrayList;
import java.util.List;

public class StructuredMessage extends ChatMessage
{
	protected List<String> dataList = new ArrayList();
	
	protected StructuredMessage(String type) 
	{
		super(type);
	}
	
	protected StructuredMessage(String type, List dataList)
	{
		super(type);
		this.dataList = dataList;
	}
	
	protected List getList()
	{
		return this.dataList;
	}
	
}
