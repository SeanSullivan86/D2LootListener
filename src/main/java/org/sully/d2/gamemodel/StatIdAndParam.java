package org.sully.d2.gamemodel;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.staticgamedata.D2ItemStat;

@Value
@Builder
public class StatIdAndParam {
	int statId;
	int param;
	
	public StatIdAndParam(String statCode, int param) {
		this(D2ItemStat.fromCode(statCode).getId(), param);
	}
	
	public StatIdAndParam(int statId, int param) {
		this.statId = statId;
		this.param = param;
	}

}
