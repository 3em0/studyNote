System.out.println("event occurs");
synchronized (listener) {
	listener.onEvent(new EventObject(this));
}