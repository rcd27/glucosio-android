package org.glucosio.android.presenter;

import org.glucosio.android.activity.AddGlucoseActivity;
import org.glucosio.android.db.DatabaseHandler;
import org.glucosio.android.db.GlucoseReading;
import org.glucosio.android.db.User;
import org.glucosio.android.report.CrashReporter;
import org.glucosio.android.tools.ReadingTools;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddGlucosePresenterTest {

    @Mock
    private AddGlucoseActivity mockActivity;

    @Mock
    private DatabaseHandler dB;

    @Mock
    private ReadingTools readingTools;

    @Mock
    private CrashReporter mockCrashReporter;

    @InjectMocks
    private AddGlucosePresenter presenter;

    @Mock
    private User userMock;

    private final static String FAKE_TIME = "11:09";
    private final static String FAKE_DATE = "22.12";
    private final static String FAKE_TYPE = "fakeType";
    private final static String FAKE_READING_WRONG = "2562";
    private final static String FAKE_READING_OK = "500";

    private final Date fakeDate = new Date();

    @Before
    public void setUp() {
        presenter.updateReadingSplitDateTime(fakeDate);
        when(dB.getUser(anyLong())).thenReturn(userMock);
    }

    @Test
    public void dialogOnButtonPressed_numberIsNull() {
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, "", FAKE_TYPE, "");
        verify(mockActivity).showErrorMessage();
    }

    @Test
    public void dialogOnButtonPressed_isReadingAdded_false() {
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, FAKE_READING_WRONG, FAKE_TYPE, "");
        verify(mockActivity).showDuplicateErrorMessage();
    }

    @Test
    public void dialogOnButtonPressed_isReadingAdded_true_oldIdIsUlnown() {
        when(dB.addGlucoseReading(Mockito.any(GlucoseReading.class))).thenReturn(true);
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, FAKE_READING_WRONG, FAKE_TYPE, "");
        verify(mockActivity).finishActivity();
    }

    @Test
    public void dialogOnButtonPressed_isReadingAdded_true_oldIdIsAnyInt() {
        when(dB.editGlucoseReading(anyInt(), (GlucoseReading) Mockito.any())).thenReturn(true);
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, FAKE_READING_WRONG, FAKE_TYPE, "", 165165);
        verify(mockActivity).finishActivity();
    }

    @Test
    public void dialogOnButtonPressed_validationFailed() {
        // this case works only if db returns "mmol/L"
        when(userMock.getPreferred_unit()).thenReturn("mmol/L");
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, "1000", FAKE_TYPE, "");
        verify(mockActivity).showErrorMessage();
    }

    @Test
    public void updateSpinnerTypeTime() {
        when(readingTools.hourToSpinnerType(anyInt())).thenReturn(anyInt());
        presenter.updateSpinnerTypeTime();
        verify(mockActivity).updateSpinnerTypeTime(anyInt());
    }

    @Test
    public void validateGlucose_mgDl_true_and_readingIsOk() {
        when(userMock.getPreferred_unit()).thenReturn("mg/dL");
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, FAKE_READING_OK, FAKE_TYPE, "");
        verify(mockActivity).showDuplicateErrorMessage();
    }

    @Test
    public void validateGlucose_mgDl_true_and_readingIsWrong() {
        when(userMock.getPreferred_unit()).thenReturn("mg/dL");
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, FAKE_READING_WRONG, FAKE_TYPE, "");
        verify(mockActivity).showErrorMessage();
    }

    @Test
    public void validateGlucose_mgDl_true_and_readingIsWrongLess() {
        when(userMock.getPreferred_unit()).thenReturn("mg/dL");
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, "15", FAKE_TYPE, "");
        verify(mockActivity).showErrorMessage();
    }

    @Test
    public void validateGlucose_parseIntException() {
        when(userMock.getPreferred_unit()).thenReturn("mg/dL");
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, "abc123", FAKE_TYPE, "");
        verify(mockCrashReporter).log("Exception during reading validation");
        verify(mockCrashReporter).report((Throwable) any());
    }


    @Test
    public void validateGlucose_parseDoubleException() {
        when(userMock.getPreferred_unit()).thenReturn("mmol/L");
        presenter.dialogOnAddButtonPressed(FAKE_TIME, FAKE_DATE, "abc123", FAKE_TYPE, "");
        verify(mockCrashReporter).log("Exception during reading validation");
        verify(mockCrashReporter).report((Throwable) any());
    }

    @Test
    public void retrieveSpinnerID_found() {
        final String fakeMeasureTypeText = "fake_measure";
        List<String> fakeMeasuredTypeList = new ArrayList<String>() {{
            add("fakeMeasure11");
            add("glucosio");
            add(fakeMeasureTypeText);
        }};
        int result = presenter.retrieveSpinnerID(fakeMeasureTypeText, fakeMeasuredTypeList);

        assertEquals(2, result);
    }

    @Test
    public void retrieveSpinnerID_notFound() {
        final String fakeMeasureTypeText = "fake_measure";
        List<String> fakeMeasuredTypeList = new ArrayList<String>() {{
            add("fakeMeasure11");
            add("glucosio");
            add("there is no measure here");
        }};

        assertNull(presenter.retrieveSpinnerID(fakeMeasureTypeText, fakeMeasuredTypeList));
    }
}
