package com.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.entity.Pharmacy;

import org.simpleflatmapper.csv.CsvParser;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

// References: https://gist.github.com/arnaudroger/7cbb9ca1acda66341fc10bf54ab01439
@Component
public class MaskHandler {
	private static final String dataURL = "https://data.nhi.gov.tw/Datasets/Download.ashx?rid=A21030000I-D50001-001&l=https://data.nhi.gov.tw/resource/mask/maskdata.csv";
	// private static final String fileName = "maskdata.csv"; // if the link is unavailable

	private List<Pharmacy> pharmacyList;

	private Map<String, String> constructFieldNameTranslationMap() {
		Map<String, String> fieldNameTranslationMap = new HashMap<String, String>();
		fieldNameTranslationMap.put("﻿醫事機構代碼", "id");
		fieldNameTranslationMap.put("醫事機構名稱", "name");
		fieldNameTranslationMap.put("醫事機構地址", "address");
		fieldNameTranslationMap.put("醫事機構電話", "phone");
		fieldNameTranslationMap.put("成人口罩剩餘數", "numberOfAdultMasks");
		fieldNameTranslationMap.put("兒童口罩剩餘數", "numberOfChildrenMasks");
		fieldNameTranslationMap.put("來源資料時間", "updatedTime");

		return fieldNameTranslationMap;
	}

	public String produceStringFromURL(String requestURL) throws IOException {
		try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	public String produceStringFromFile(String fileName) throws IOException {
		InputStream is = new FileInputStream(fileName);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		buf.close();
		return sb.toString();
	}

	public String produceDataJson(String csvContent) throws IOException, URISyntaxException {

		Map<String, String> fieldNameTranslationMap = constructFieldNameTranslationMap();
		org.simpleflatmapper.lightningcsv.CsvReader reader = CsvParser.reader(csvContent);

		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Iterator<String[]> iterator = reader.iterator();
		String[] headers = iterator.next();

		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(new PrintStream(output))) {

			jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
			jsonGenerator.writeStartArray();

			while (iterator.hasNext()) {
				jsonGenerator.writeStartObject();
				String[] values = iterator.next();
				int nbCells = Math.min(values.length, headers.length);
				for (int i = 0; i < nbCells; i++) {
					jsonGenerator.writeFieldName(fieldNameTranslationMap.get(headers[i]));
					jsonGenerator.writeString(values[i]);
				}
				jsonGenerator.writeEndObject();
			}
			jsonGenerator.writeEndArray();
		}

		return output.toString();
	}

	public List<Pharmacy> convertToObjects(String jsonData) {

		Gson gson = new Gson();
		ArrayList<Pharmacy> clinicList = new ArrayList<Pharmacy>();

		try {
			Type listType = new TypeToken<List<Pharmacy>>() {
			}.getType();
			clinicList = gson.fromJson(jsonData, listType);
		} catch (Exception e) {
			System.err.println("Exception: " + e);
		}
		return clinicList;
	}

	public List<Pharmacy> findPharmacies(String queryName, String queryAddress) {
		List<Pharmacy> matchingElements;
		if (queryName.equals("") && queryAddress.equals("")) {
			matchingElements = pharmacyList.stream().collect(Collectors.toList());

		} else if (queryName.equals("")) {
			matchingElements = pharmacyList.stream().filter(str -> str.getAddress().trim().contains(queryAddress))
					.collect(Collectors.toList());
		} else if (queryAddress.equals("")) {
			matchingElements = pharmacyList.stream().filter(str -> str.getName().trim().contains(queryName))
					.collect(Collectors.toList());
		} else {
			matchingElements = pharmacyList.stream().filter(
					str -> str.getName().trim().contains(queryName) && str.getAddress().trim().contains(queryAddress))
					.collect(Collectors.toList());
		}

		return matchingElements;
	}

	public Pharmacy getPharmacy(String pharmacyId) {
		List<Pharmacy> matchingElements = pharmacyList.stream().filter(str -> str.getId().equals(pharmacyId))
				.collect(Collectors.toList());
		if ((matchingElements != null) && (matchingElements.size() > 0)) {
			return matchingElements.get(0);
		} else {
			return null;
		}
	}

	@PostConstruct
	public void initialize() throws IOException, URISyntaxException {
		String maskData = produceStringFromURL(dataURL);
		// String maskData = produceStringFromFile(fileName);
		String maskDataJson = produceDataJson(maskData);
		pharmacyList = convertToObjects(maskDataJson);
	}

}
