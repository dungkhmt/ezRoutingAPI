package server;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import io.spring.guides.gs_producing_web_service.GetComputeRouteRequest;
import io.spring.guides.gs_producing_web_service.GetComputeRouteResponse;
import io.spring.guides.gs_producing_web_service.GetCountryRequest;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;
import io.spring.guides.gs_producing_web_service.Routes;

@Endpoint
public class WSEndpoint {
	private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

	private WSRepository countryRepository;

	@Autowired
	public WSEndpoint(WSRepository countryRepository) {
		this.countryRepository = countryRepository;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCountryRequest")
	@ResponsePayload
	public GetCountryResponse getCountry(@RequestPayload GetCountryRequest request) {
		GetCountryResponse response = new GetCountryResponse();
		response.setCountry(countryRepository.findCountry(request.getName()));

		return response;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getComputeRouteRequest")
	@ResponsePayload
	public GetComputeRouteResponse getComputeRoutes(@RequestPayload GetComputeRouteRequest request) {
		GetComputeRouteResponse response = new GetComputeRouteResponse();

		// Preparing response data
		response.setTotalDistance(new BigDecimal(100.10)); 		// Set value TotalDistance
		response.setViolations(10);		// Set value Violation
		
		// Set value Routes
		Routes routes = new Routes();
		routes.setId(1);
		routes.setId(2);
		routes.setId(3);
		response.getRoutes().add(routes);
		
		return response;
	}
}