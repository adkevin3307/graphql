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
            Map<String, Object> argument = dataFetchingEnvironment.getArguments();
            if (argument.containsKey("filter")) {
                String filter_string = argument.get("filter").toString().replace('=', ':');
                JSONObject filter = new JSONObject(filter_string);

                Stream<Pharmacy> result = pharmacies.stream();

                if (filter.has("id")) {
                    result = result.filter(pharmacy -> pharmacy.getId().contains(filter.get("id").toString()));
                }
                if (filter.has("name")) {
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
}