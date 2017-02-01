package nio.multiplexer;

import hu.qgears.commons.UtilEvent;

public interface IMultiplexer {

	public final UtilEvent<Exception> closedEvent = new UtilEvent<>();

	void register(MultiplexerSender multiplexerSender);

	void dataAvailable(MultiplexerSender multiplexerSender);

	void remove(MultiplexerSender multiplexerSender);

	void register(MultiplexerReceiver multiplexerReceiver, int id);

	void remove(MultiplexerReceiver multiplexerReceiver);

}
