package org.mtransit.parser.ca_niagara_falls_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// https://niagaraopendata.ca/dataset/niagara-region-transit-gtfs
// https://maps.niagararegion.ca/googletransit/NiagaraRegionTransit.zip
public class NiagaraFallsTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-niagara-falls-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new NiagaraFallsTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Niagara Falls Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating Niagara Falls Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String NIAGARA_FALLS_TRANSIT = "Niagara Falls Transit";

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (gRoute.getRouteShortName().equals("22") //
				|| gRoute.getRouteLongName().toLowerCase(Locale.ENGLISH).contains("erie")) {
			return true;
		}
		if (gRoute.getRouteShortName().contains("WEGO") //
				|| gRoute.getRouteLongName().contains("WEGO")) {
			return true;
		}
		if (gRoute.getRouteLongName().equals("604 - Orange - NOTL")) {
			return true;
		}
		if (!gRoute.getAgencyId().contains(NIAGARA_FALLS_TRANSIT)) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	private static final String AGENCY_COLOR_GREEN = "B2DA18"; // GREEN (from PDF Service Schedule)
	@SuppressWarnings("unused")
	private static final String AGENCY_COLOR_BLUE = "233E76"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
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
			System.out.printf("\nUnexpected route color for %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static final Pattern STARTS_WITH_ROUTE_RID = Pattern.compile("(^(rte|route) [\\d]+)", Pattern.CASE_INSENSITIVE);

	private String cleanRouteLongName(String routeLongName) {
		routeLongName = STARTS_WITH_ROUTE_RID.matcher(routeLongName).replaceAll(StringUtils.EMPTY);
		return routeLongName;
	}

	private static final String SQUARE = "Square";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = cleanRouteLongName(gRoute.getRouteLongName());
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
			System.out.printf("\nUnexpected route long name for %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
		return routeLongName;
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	public static final Pattern STARTS_WITH_NF_A00_ = Pattern.compile("((^){1}((nf\\_[A-Z]{1,3}[\\d]{2,4}(\\_)?)+([A-Z]{3}(stop))?(stop|sto)?))",
			Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopOriginalId(String gStopId) {
		gStopId = STARTS_WITH_NF_A00_.matcher(gStopId).replaceAll(StringUtils.EMPTY);
		return gStopId;
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	private static final Pattern STARTS_WITH_RSN_ = Pattern.compile("(^[\\d]+( )?)", Pattern.CASE_INSENSITIVE);
	private static final Pattern STARTS_WITH_RLN_DASH = Pattern.compile("(^[^\\-]+\\-)", Pattern.CASE_INSENSITIVE);
	private static final Pattern STARTS_WITH_RLN_SLASH = Pattern.compile("(^[^\\/]+\\/)", Pattern.CASE_INSENSITIVE);

	private static final Pattern AND_NO_SPACE = Pattern.compile("(([\\S])(\\&)([\\S]))", Pattern.CASE_INSENSITIVE);
	private static final String AND_NO_SPACE_REPLACEMENT = "$2" + "&" + "$4";

	private static final Pattern SQUARE_ = Pattern.compile("((^|\\W){1}(sqaure)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String SQUARE_REPLACEMENT = "$2" + SQUARE + "$4";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = STARTS_WITH_RSN_.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_RLN_DASH.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_RLN_SLASH.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = SQUARE_.matcher(tripHeadsign).replaceAll(SQUARE_REPLACEMENT);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = AND_NO_SPACE.matcher(tripHeadsign).replaceAll(AND_NO_SPACE_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign); // 1st
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 102L) {
			if (Arrays.asList( //
					"NF Bus Terminal", //
					"Main & Ferry Hub" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry Hub", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 104L) {
			if (Arrays.asList( //
					"Bus Terminal", // <>
					"Main & Ferry" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"NF Bus Terminal", //
					"Main & Ferry Hub" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry Hub", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 106L) {
			if (Arrays.asList( //
					"Inb1", //
					"Main & Ferry" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Gunning & Willloughby", //
					"Main & Ferry Hub" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry Hub", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Aillanthus Ave", //
					"Main & Ferr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 108L) {
			if (Arrays.asList( //
					"Bus Term", //
					"Bus Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Bus Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 112L) {
			if (Arrays.asList( //
					"McLeod Rd.", //
					"Niagara Sq" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Niagara Sq", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Gunning & Willloughby", //
					"Niagara Sq A" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Niagara Sq A", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 113L) {
			if (Arrays.asList( //
					"Brown Rd. Loop", //
					"Niagara Squar" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Niagara Squar", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 203L) {
			if (Arrays.asList( //
					"NF Bus Terminal", //
					"Main & Ferry Hub" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry Hub", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 204L) {
			if (Arrays.asList( //
					"NF Bus Terminal", //
					"Main & Ferry Hub" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry Hub", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 206L) {
			if (Arrays.asList( //
					"Main & Ferry", // <> #204
					"Chippawa" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Chippawa", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Bus Terminal", //
					"Main & Ferry" // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"W Corner", //
					"Portage Rd & Front St(Tims)" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Portage Rd & Front St(Tims)", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"NF Bus Terminal", //
					"Main & Ferry Hub" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Main & Ferry Hub", mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName); // 1st
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@NotNull
	@Override
	public String getStopCode(GStop gStop) {
		if (ZERO_0.equals(gStop.getStopCode())) {
			return StringUtils.EMPTY;
		}
		return super.getStopCode(gStop);
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final String ZERO_0 = "0";

	@Override
	public int getStopId(GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (stopCode == null || stopCode.length() == 0 || ZERO_0.equals(stopCode)) {
			stopCode = gStop.getStopId();
		}
		stopCode = STARTS_WITH_NF_A00_.matcher(stopCode).replaceAll(StringUtils.EMPTY);
		if (Utils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // using stop code as stop ID
		}
		int digits = 0;
		Matcher matcher = DIGITS.matcher(stopCode);
		if (matcher.find()) {
			digits = Integer.parseInt(matcher.group());
		} else {
			if (stopCode.equals("Por&Burn")) {
				return 1_000_001;
			} else if (stopCode.equals("Por&Mlnd")) {
				return 1_000_002;
			} else if (stopCode.equals("Temp")) {
				return 6_200_000;
			} else {
				System.out.printf("\nStop doesn't have an ID! %s (stopCode:%s)\n", gStop, stopCode);
				System.exit(-1);
				return -1;
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
			System.out.printf("\nStop doesn't have an ID (ends with)! %s (stopCode:%s)\n", gStop, stopCode);
			System.exit(-1);
			return -1;
		}
		return stopId + digits;
	}
}
