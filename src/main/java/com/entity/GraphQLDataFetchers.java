package com.entity;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.model.MaskHandler;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@SuppressWarnings("rawtypes")
@Component
public class GraphQLDataFetchers {
    @Autowired
    private MaskHandler handler;
    private static List<Pharmacy> pharmacies;

    public GraphQLDataFetchers(MaskHandler handler) {
        this.handler = handler;

        pharmacies = this.handler.findPharmacies("", "");
    }

    public DataFetcher getPharmaciesDataFetcher() {
        return dataFetchingEnvironment -> {
            if (dataFetchingEnvironment.containsArgument("filter")) {
                Map<String, String> filter = dataFetchingEnvironment.getArgument("filter");

                Stream<Pharmacy> result = pharmacies.stream();

                if (filter.containsKey("id")) {
                    result = result.filter(pharmacy -> pharmacy.getId().contains(filter.get("id").toString()));
                }
                if (filter.containsKey("name")) {
                    result = result.filter(pharmacy -> pharmacy.getName().contains(filter.get("name").toString()));
                }

                return result.toArray();
            }

            return pharmacies;
        };
    }

    public DataFetcher getPharmacyIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String id = dataFetchingEnvironment.getArgument("id");
            return pharmacies.stream().filter(pharmacy -> pharmacy.getId().equals(id)).findFirst().orElse(null);
        };
    }

    public DataFetcher getPharmacyNameDataFetcher() {
        return dataFetchingEnvironment -> {
            String name = dataFetchingEnvironment.getArgument("name");
            return pharmacies.stream().filter(pharmacy -> pharmacy.getName().equals(name)).findFirst().orElse(null);
        };
    }

    public DataFetcher updatePharmacyDataFetcher() {
        return dataFetchingEnvironment -> {
            String id = dataFetchingEnvironment.getArgument("id");
            String input_string = dataFetchingEnvironment.getArgument("input").toString().replace('=', ':');
            JSONObject input = new JSONObject(input_string);

            for (int i = 0; i < pharmacies.size(); i++) {
                if (pharmacies.get(i).getId().equals(id)) {
                    pharmacies.get(i).setName(input.get("name").toString());
                }
            }

            return pharmacies.stream().filter(pharmacy -> pharmacy.getId().equals(id)).findFirst();
        };
    }
}