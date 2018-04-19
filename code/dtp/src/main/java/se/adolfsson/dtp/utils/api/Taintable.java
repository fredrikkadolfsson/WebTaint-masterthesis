package se.adolfsson.dtp.utils.api;

public interface Taintable {
	void setTaint(boolean value);

	boolean isTainted();
}
