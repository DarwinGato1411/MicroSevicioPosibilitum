package com.ec.g2g.quickbook;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ec.g2g.global.ValoresGlobales;
import com.intuit.ipp.data.TaxCode;
import com.intuit.ipp.data.TaxRate;
import com.intuit.ipp.data.TaxRateDetail;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;

@Service
public class TaxCodeQB {
	@Autowired
	public QBOServiceHelper helper;
	@Autowired
	private ValoresGlobales valoresGlobales;

	@Autowired
	ManejarToken manejarToken;

	public TaxCode obtenerTaxCode(String id) {
		String realmId = valoresGlobales.REALMID;
		// String accessToken = valoresGlobales.TOKEN;
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
		try {
			if (valoresGlobales.REALMID != null && valoresGlobales.REFRESHTOKEN != null) {
				// get DataService
				DataService service = helper.getDataService(realmId, accessToken);
				String sql = "select * From TaxCode where id='" + id + "'";
				System.out.println("QUERY TAXCODE " + sql);
				QueryResult queryResult = service.executeQuery(sql);
				return (TaxCode) queryResult.getEntities().get(0);

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		return null;
	}

	public List<TaxRate> obtenerTaxRateDetail(String id) {
		String realmId = valoresGlobales.REALMID;
		// String accessToken = valoresGlobales.TOKEN;
		String accessToken = manejarToken.refreshToken(valoresGlobales.REFRESHTOKEN);
		try {
			if (valoresGlobales.REALMID != null && valoresGlobales.REFRESHTOKEN != null) {
				// get DataService
				DataService service = helper.getDataService(realmId, accessToken);
				String sql = "Select * From TaxRate where id='" + id + "'";
				System.out.println("QUERY TaxRate " + sql);
				QueryResult queryResult = service.executeQuery(sql);
				return (List<TaxRate>) queryResult.getEntities();

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		return null;
	}
}
