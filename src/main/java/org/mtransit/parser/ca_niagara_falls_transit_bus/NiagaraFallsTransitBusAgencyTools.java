package org.mtransit.parser.ca_niagara_falls_transit_bus;

import static org.mtransit.parser.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://niagaraopendata.ca/dataset/niagara-region-transit-gtfs
// https://maps.niagararegion.ca/googletransit/NiagaraRegionTransit.zip
// https://niagaraopendata.ca/dataset/1a1b885e-1a86-415d-99aa-6803a2d8f178/resource/52c8cd46-d976-4d57-990f-e8018bcd27cb/download/gtfs.zip
public class NiagaraFallsTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new NiagaraFallsTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Niagara Falls Transit";
	}

	private static final String NIAGARA_FALLS_TRANSIT = "Niagara Falls Transit";

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		final String rln = gRoute.getRouteLongNameOrDefault();
		final String rsnS = gRoute.getRouteShortName();
		if (rsnS.equals("22")
				|| rln.toLowerCase(Locale.ENGLISH).contains("erie")) {
			return EXCLUDE;
		}
		if (rsnS.contains("WEGO") //
				|| rln.contains("WEGO")) {
			return EXCLUDE;
		}
		if (rln.equals("604 - Orange - NOTL")) {
			return EXCLUDE;
		}
		//noinspection deprecation
		final String agencyId = gRoute.getAgencyIdOrDefault();
		final boolean allAgencies = agencyId.contains("AllNRT_") || agencyId.equals("1");
		if (!agencyId.contains(NIAGARA_FALLS_TRANSIT)
				&& !allAgencies) {
			return EXCLUDE;
		}
		if (allAgencies) {
			if (!CharUtils.isDigitsOnly(rsnS)) {
				return EXCLUDE;
			}
			final int rsn = Integer.parseInt(rsnS);
			if (rsn < 100 || rsn > 299) {
				return EXCLUDE;
			}
		}
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	private static final String AGENCY_COLOR_GREEN = "B2DA18"; // GREEN (from PDF Service Schedule)
	// private static final String AGENCY_COLOR_BLUE = "233E76"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case 101: return "F57215";
			case 102: return "2E3192";
			case 103: return "EC008C";
			case 104: return "19B5F1";
			case 105: return "ED1C24";
			case 106: return "BAA202";
			case 107: return "A05843";
			case 108: return "008940";
			case 109: return "66E530";
			case 110: return "4372C2";
			case 111: return "F24D3E";
			case 112: return "9E50AE";
			case 113: return "724A36";
			case 114: return "B30E8E";
			//
			case 203: return "EC008C";
			case 204: return "19B5F1";
			case 205: return "ED1C24";
			case 206: return "BAA202";
			//
			case 209: return "66C530";
			case 210: return "4372C2";
			case 211: return "F24D3E";
			//
			case 213: return "724A36";
			case 214: return "B30E8E";
			// @formatter:on
			}
			throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
		}
		return super.getRouteColor(gRoute);
	}

	private static final Pattern STARTS_WITH_ROUTE_RID = Pattern.compile("(^(rte|route) [\\d]+)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = STARTS_WITH_ROUTE_RID.matcher(routeLongName).replaceAll(EMPTY);
		return routeLongName;
	}

	private static final String SQUARE = "Square";

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = cleanRouteLongName(gRoute.getRouteLongNameOrDefault());
		if (StringUtils.isEmpty(routeLongName)) {
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case 101: return "Dunn St";
			case 102: return "Morrison & Dorchester"; // Hospital ?
			case 103: return "Drummond Rd";
			case 104: return "Victoria Ave";
			case 105: return "Kalar Rd";
			case 106: return "Ailanthus Ave";
			case 107: return "Town & County Plz";
			case 108: return "Thorold Stone Rd";
			case 109: return "Thorold Stone Rd";
			case 110: return "Drummond Rd";
			case 111: return "Dorchester Rd";
			case 112: return "McLeod Rd";
			case 113: return "Montrose Rd";
			case 114: return "Town & County Plz";
			//
			case 203: return "Drummond Rd";
			case 204: return "Victoria Ave";
			case 205: return "Kalar Rd";
			case 206: return "Ailanthus Ave";
			//
			case 209: return "Thorold Stone Rd";
			case 210: return "Hospital";
			case 211: return "Dorchester Rd";
			//
			case 213: return "Montrose Rd ";
			case 214: return "Town & County Plz";
			// @formatter:on
			}
			throw new MTLog.Fatal("Unexpected route long name for %s!", gRoute);
		}
		return routeLongName;
	}

	private static final Pattern STARTS_WITH_NF_A00_ = Pattern.compile("((^)(((nf|nft)_[a-z]{1,3}[\\d]{2,4}(_)?)+([a-z]{3}(stop))?(stop|sto)?))",
			Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = STARTS_WITH_NF_A00_.matcher(gStopId).replaceAll(EMPTY);
		return gStopId;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_RSN_ = Pattern.compile("(^[\\d]+( )?)", Pattern.CASE_INSENSITIVE);
	private static final Pattern STARTS_WITH_RLN_DASH = Pattern.compile("(^[^\\-]+-)", Pattern.CASE_INSENSITIVE);

	private static final Pattern AND_NO_SPACE = Pattern.compile("(([\\S])\\s?([&@])\\s?([\\S]))", Pattern.CASE_INSENSITIVE);
	private static final String AND_NO_SPACE_REPLACEMENT = "$2 $3 $4";

	private static final Pattern SQUARE_ = Pattern.compile("((^|\\W)(sqaure)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String SQUARE_REPLACEMENT = "$2" + SQUARE + "$4";

	private static final Pattern ENDS_WITH_ARROW_TERM_ = Pattern.compile("(.* -> terminal$)", Pattern.CASE_INSENSITIVE);
	private static final String ENDS_WITH_ARROW_TERM_REPLACEMENT = "Bus Terminal";

	private static final Pattern STARTS_WITH_ARROWS_ = Pattern.compile("((^|^.* )(>>) )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = AND_NO_SPACE.matcher(tripHeadsign).replaceAll(AND_NO_SPACE_REPLACEMENT);
		tripHeadsign = ENDS_WITH_ARROW_TERM_.matcher(tripHeadsign).replaceAll(ENDS_WITH_ARROW_TERM_REPLACEMENT);
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign);
		tripHeadsign = STARTS_WITH_RSN_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = STARTS_WITH_RLN_DASH.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = SQUARE_.matcher(tripHeadsign).replaceAll(SQUARE_REPLACEMENT);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = STARTS_WITH_ARROWS_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign); // 1st
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = AND_NO_SPACE.matcher(gStopName).replaceAll(AND_NO_SPACE_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName); // 1st
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		if (ZERO_0.equals(gStop.getStopCode())) {
			return EMPTY;
		}
		return super.getStopCode(gStop);
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final String ZERO_0 = "0";

	@Override
	public int getStopId(@NotNull GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (stopCode.length() == 0 || ZERO_0.equals(stopCode)) {
			//noinspection deprecation
			stopCode = gStop.getStopId();
		}
		stopCode = STARTS_WITH_NF_A00_.matcher(stopCode).replaceAll(EMPTY);
		if (CharUtils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // using stop code as stop ID
		}
		int digits;
		Matcher matcher = DIGITS.matcher(stopCode);
		if (matcher.find()) {
			digits = Integer.parseInt(matcher.group());
		} else {
			switch (stopCode) {
			case "Por&Burn":
				return 1_000_001;
			case "Por&Mlnd":
				return 1_000_002;
			case "Temp":
				return 6_200_000;
			default:
				throw new MTLog.Fatal("Stop doesn't have an ID! %s (stopCode:%s)", gStop, stopCode);
			}
		}
		int stopId;
		String stopCodeLC = stopCode.toLowerCase(Locale.ENGLISH);
		if (stopCodeLC.endsWith("a")) {
			stopId = 100_000;
		} else if (stopCodeLC.endsWith("b")) {
			stopId = 200_000;
		} else if (stopCodeLC.endsWith("c")) {
			stopId = 300_000;
		} else if (stopCodeLC.endsWith("in")) {
			stopId = 5_000_000;
		} else if (stopCodeLC.endsWith("out")) {
			stopId = 5_100_000;
		} else if (stopCodeLC.endsWith("temp10")) {
			stopId = 6_100_000;
		} else {
			throw new MTLog.Fatal("Stop doesn't have an ID (ends with)! %s (stopCode:%s)", gStop, stopCode);
		}
		return stopId + digits;
	}
}
