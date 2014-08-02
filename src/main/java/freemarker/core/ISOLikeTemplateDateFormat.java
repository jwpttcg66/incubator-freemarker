package freemarker.core;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DateUtil;
import freemarker.template.utility.DateUtil.CalendarFieldsToDateConverter;
import freemarker.template.utility.DateUtil.DateParseException;
import freemarker.template.utility.DateUtil.DateToISO8601CalendarFactory;
import freemarker.template.utility.StringUtil;

abstract class ISOLikeTemplateDateFormat  extends TemplateDateFormat {

    private static final String XS_LESS_THAN_SECONDS_ACCURACY_ERROR_MESSAGE
            = "Less than seconds accuracy isn't allowed by the XML Schema format";
    private final ISOLikeTemplateDateFormatFactory factory; 
    protected final int dateType;
    protected final TimeZone timeZone;
    protected final boolean useUTC;
    protected final Boolean showZoneOffset;
    protected final int accuracy;

    /**
     * @param settingValue The value of the ..._format setting, like "iso nz".
     * @param parsingStart The index of the char in the {@code settingValue} that directly after the prefix that has
     *     indicated the exact formatter class (like "iso" or "xs") 
     * @param factory 
     */
    public ISOLikeTemplateDateFormat(
            String settingValue, int parsingStart,
            int dateType, TimeZone timeZone,
            ISOLikeTemplateDateFormatFactory factory)
            throws ParseException, UnknownDateTypeFormattingUnsupportedException {
        this.factory = factory;
        if (dateType == TemplateDateModel.UNKNOWN) {
            throw new UnknownDateTypeFormattingUnsupportedException();
        }
        this.dateType = dateType;
        
        final int ln = settingValue.length();
        boolean afterSeparator = false;
        int i = parsingStart;
        int accuracy = DateUtil.ACCURACY_MILLISECONDS;
        Boolean showZoneOffset = null;
        boolean useUTC = false;
        while (i < ln) {
            final char c = settingValue.charAt(i++);
            if (c == '_' || c == ' ') {
                afterSeparator = true;
            } else {
                if (!afterSeparator) {
                    throw new java.text.ParseException(
                            "Missing space or \"_\" before \"" + c + "\"", i);
                }
                
                switch (c) {
                case 'h':
                case 'm':
                case 's':
                    if (accuracy != DateUtil.ACCURACY_MILLISECONDS) {
                        throw new java.text.ParseException(
                                "Character \"" + c + "\" is unexpected as accuracy was already specified earlier." , i);
                    }
                    switch (c) {
                    case 'h':
                        if (isXSMode()) {
                            throw new java.text.ParseException(XS_LESS_THAN_SECONDS_ACCURACY_ERROR_MESSAGE, i);
                        }
                        accuracy = DateUtil.ACCURACY_HOURS;
                        break;
                    case 'm':
                        if (i < ln && settingValue.charAt(i) == 's') {
                            i++;
                            accuracy = DateUtil.ACCURACY_MILLISECONDS_FORCED;
                        } else {
                            if (isXSMode()) {
                                throw new java.text.ParseException(XS_LESS_THAN_SECONDS_ACCURACY_ERROR_MESSAGE, i);
                            }
                            accuracy = DateUtil.ACCURACY_MINUTES;
                        }
                        break;
                    case 's':
                        accuracy = DateUtil.ACCURACY_SECONDS;
                        break;
                    }
                    break;
                case 'n':
                case 'f':
                    if (showZoneOffset != null) {
                        throw new java.text.ParseException(
                                "Character \"" + c + "\" is unexpected as zone offset visibility was already "
                                + "specified earlier." , i);
                    }
                    switch (c) {
                    case 'n':
                        if (i < ln && settingValue.charAt(i) == 'z') {
                            i++;
                            showZoneOffset = Boolean.FALSE;
                        } else {
                            throw new java.text.ParseException("\"n\" must be followed by \"z\"", i);
                        }
                        break;
                    case 'f':
                        if (i < ln && settingValue.charAt(i) == 'z') {
                            i++;
                            showZoneOffset = Boolean.TRUE;
                        } else {
                            throw new java.text.ParseException("\"f\" must be followed by \"z\"", i);
                        }
                        break;
                    }
                    break;
                case 'u':
                    if (useUTC) {
                        throw new java.text.ParseException(
                                "Character \"" + c + "\" was already used earlier." , i);
                    }
                    useUTC = true;
                    break;
                default:
                    throw new java.text.ParseException(
                            "Unexpected character, " + StringUtil.jQuote(String.valueOf(c))
                            + ". Expected the beginning of one of: h, m, s, ms, nz, fz, u",
                            i);
                } // switch
                afterSeparator = false;
            } // else
        } // while
        
        this.accuracy = accuracy;
        this.showZoneOffset = showZoneOffset;
        this.useUTC = useUTC;
        this.timeZone = timeZone;
    }
    
    public final String format(TemplateDateModel dateModel, boolean zonelessInput) throws TemplateModelException {
        final Date date = dateModel.getAsDate();
        return format(
                date,
                dateType != TemplateDateModel.TIME,
                dateType != TemplateDateModel.DATE,
                showZoneOffset == null
                        ? !zonelessInput
                        : showZoneOffset.booleanValue(),
                accuracy,
                zonelessInput || !useUTC ? timeZone : DateUtil.UTC,
                factory.getISOBuiltInCalendar());
    }
    
    protected abstract String format(Date date,
            boolean datePart, boolean timePart, boolean offsetPart,
            int accuracy,
            TimeZone timeZone,
            DateToISO8601CalendarFactory calendarFactory);

    public final Date parse(String s) throws java.text.ParseException {
        CalendarFieldsToDateConverter calToDateConverter = factory.getCalendarFieldsToDateCalculator();
        TimeZone tz = useUTC ? DateUtil.UTC : timeZone;
        if (dateType == TemplateDateModel.DATE) {
            return parseDate(s, tz, calToDateConverter);
        } else if (dateType == TemplateDateModel.TIME) {
            return parseTime(s, tz, calToDateConverter);
        } else if (dateType == TemplateDateModel.DATETIME) {
            return parseDateTime(s, tz, calToDateConverter);
        } else {
            throw new BugException("Unexpected date type: " + dateType);
        }
    }
    
    protected abstract Date parseDate(
            String s, TimeZone tz,
            CalendarFieldsToDateConverter calToDateConverter) 
            throws DateParseException;
    
    protected abstract Date parseTime(
            String s, TimeZone tz,
            CalendarFieldsToDateConverter calToDateConverter) 
            throws DateParseException;
    
    protected abstract Date parseDateTime(
            String s, TimeZone tz,
            CalendarFieldsToDateConverter calToDateConverter) 
            throws DateParseException;

    public final String getDescription() {
        switch (dateType) {
            case TemplateDateModel.DATE: return getDateDescription();
            case TemplateDateModel.TIME: return getTimeDescription();
            case TemplateDateModel.DATETIME: return getDateTimeDescription();
            default: return "<error: wrong format dateType>";
        }
    }
    
    protected abstract String getDateDescription();
    protected abstract String getTimeDescription();
    protected abstract String getDateTimeDescription();
    
    public final boolean isLocaleBound() {
        return false;
    }

    protected abstract boolean isXSMode();

}
