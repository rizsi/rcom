package nio.multiplexer;

import hu.qgears.commons.UtilEvent;

public interface IMultiplexer {

	UtilEvent<Exception> getClosedEvent();

	void register(MultiplexerSender multiplexerSender);

	void dataAvailable(MultiplexerSender multiplexerSender);
	
	void availableChanged(MultiplexerReceiver receiver);

	void remove(MultiplexerSender multiplexerSender);

	void register(MultiplexerReceiver multiplexerReceiver, int id);

	void remove(MultiplexerReceiver multiplexerReceiver);

	String getUserName();

}
