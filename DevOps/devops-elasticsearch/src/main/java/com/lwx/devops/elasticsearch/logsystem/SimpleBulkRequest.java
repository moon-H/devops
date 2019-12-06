/**
 * 
 */
package com.lwx.devops.elasticsearch.logsystem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;

import java.util.HashSet;
import java.util.Set;

/**
 * @author fengql
 * @date 2019年9月5日 下午7:47:50
 * 
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SimpleBulkRequest extends BulkRequest {

	private Set<String> docIds = new HashSet<String>();
	
	public SimpleBulkRequest() {
		super();
		this.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
	}

	@Override
	public BulkRequest add(DocWriteRequest<?>... requests) {
		for (DocWriteRequest<?> request : requests) {
			docIds.add(request.id());
		}
		return super.add(requests);
	}
}
