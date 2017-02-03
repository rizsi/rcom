package com.rizsi.rcom;

import java.util.List;

public interface IVideocomCallback {

	void message(String message);

	void currentShares(List<StreamParameters> arrayList);

	void currentUsers(List<String> users);

}
