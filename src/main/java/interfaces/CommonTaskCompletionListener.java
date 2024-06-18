package interfaces;

import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.sun.istack.Nullable;

public interface CommonTaskCompletionListener {
	Response onCompletion(@Nullable JSONObject response);
}
