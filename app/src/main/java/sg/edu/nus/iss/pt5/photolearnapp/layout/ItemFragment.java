package sg.edu.nus.iss.pt5.photolearnapp.layout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.List;

import sg.edu.nus.iss.pt5.photolearnapp.R;
import sg.edu.nus.iss.pt5.photolearnapp.activity.ManageItemActivity;
import sg.edu.nus.iss.pt5.photolearnapp.constants.AppConstants;
import sg.edu.nus.iss.pt5.photolearnapp.constants.Mode;
import sg.edu.nus.iss.pt5.photolearnapp.constants.UIType;
import sg.edu.nus.iss.pt5.photolearnapp.model.Item;
import sg.edu.nus.iss.pt5.photolearnapp.model.LearningItem;
import sg.edu.nus.iss.pt5.photolearnapp.model.LearningSession;
import sg.edu.nus.iss.pt5.photolearnapp.model.QuizItem;
import sg.edu.nus.iss.pt5.photolearnapp.model.Title;
import sg.edu.nus.iss.pt5.photolearnapp.util.CommonUtils;
import sg.edu.nus.iss.pt5.photolearnapp.util.FileStoreHelper;
import sg.edu.nus.iss.pt5.photolearnapp.util.FileStoreListener;
import sg.edu.nus.iss.pt5.photolearnapp.util.SecurityContext;
import sg.edu.nus.iss.pt5.photolearnapp.util.TextToSpeechUtil;

import static sg.edu.nus.iss.pt5.photolearnapp.constants.AppConstants.RC_EDIT_ITEM;

public class ItemFragment extends Fragment implements View.OnClickListener {

    private FileStoreHelper fileStoreHelper = FileStoreHelper.getInstance();

    private LearningSession learningSession;
    private Title title;
    private Item item;
    private int position;

    private ImageView photoImageView;
    private TextView descriptionTextView;

    private CheckBox optOneCheckBox;
    private CheckBox optTwoCheckBox;
    private CheckBox optThreeCheckBox;
    private CheckBox optFourCheckBox;
    private TextView remarksTextView;

    private Button editBtn;

    private LinearLayout optLayout;

    private ImageButton textToSpeechBtn;
    private TextToSpeechUtil textToSpeechUtil;

    private TextView textViewLongitude;
    private TextView textViewLatitude;

    public static final int RequestPermissionCode = 1;

    public ItemFragment() {
        // Required empty public constructor
    }

    public static ItemFragment newInstance(int position, Title title, Item item) {

        ItemFragment fragment = new ItemFragment();

        Bundle args = new Bundle();
        args.putSerializable(AppConstants.POSITION, position);
        args.putSerializable(AppConstants.TITLE_OBJ, title);
        args.putSerializable(AppConstants.ITEM_OBJ, item);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            position = getArguments().getInt(AppConstants.POSITION);
            title = (Title) getArguments().getSerializable(AppConstants.TITLE_OBJ);
            item = (Item) getArguments().getSerializable(AppConstants.ITEM_OBJ);
        }

        textToSpeechUtil = new TextToSpeechUtil(this.getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item, container, false);

        photoImageView = (ImageView) view.findViewById(R.id.photoImageViewID);
        descriptionTextView = (TextView) view.findViewById(R.id.descriptionTextViewID);

        textViewLongitude = (TextView) view.findViewById(R.id.textViewLongitude);
        textViewLatitude = (TextView) view.findViewById(R.id.textViewLatitude);

        if (CommonUtils.isQuizUI(title)) {
            optOneCheckBox = (CheckBox) view.findViewById(R.id.optOneCheckBoxID);
            optTwoCheckBox = (CheckBox) view.findViewById(R.id.optTowCheckBoxID);
            optThreeCheckBox = (CheckBox) view.findViewById(R.id.optThreeCheckBoxID);
            optFourCheckBox = (CheckBox) view.findViewById(R.id.optFourCheckBoxID);
            remarksTextView = (TextView) view.findViewById(R.id.remarksTextViewID);
        }

        textToSpeechBtn = (ImageButton) view.findViewById(R.id.textToSpeachBtnID);
        textToSpeechBtn.setOnClickListener(this);

        initEditItemButton(view);

        populateUI();

        optLayout = (LinearLayout) view.findViewById(R.id.optLayoutID);
        optLayout.setVisibility((CommonUtils.isQuizUI(title)) ? View.VISIBLE : View.GONE);

        downloadImage();

        return view;
    }

    private void populateUI() {
        descriptionTextView.setText(item.getPhotoDesc());
        textViewLongitude.setText(Double.toString(item.getLongitude()));
        textViewLatitude.setText(Double.toString(item.getLatitude()));

        if (CommonUtils.isQuizUI(title)) {
            QuizItem quizItem = (QuizItem) item;
            optOneCheckBox.setChecked(quizItem.isOptionOneAnswer());
            optOneCheckBox.setText(quizItem.getOptionOne());
            optTwoCheckBox.setChecked(quizItem.isOptionTwoAnswer());
            optTwoCheckBox.setText(quizItem.getOptionTwo());
            optThreeCheckBox.setChecked(quizItem.isOptionThreeAnswer());
            optThreeCheckBox.setText(quizItem.getOptionThree());
            optFourCheckBox.setChecked(quizItem.isOptionFourAnswer());
            optFourCheckBox.setText(quizItem.getOptionFour());
            remarksTextView.setText(quizItem.getExplanation());
        }
    }

    private void initEditItemButton(View view) {
        editBtn = (Button) view.findViewById(R.id.editBtnID);
        if ((SecurityContext.getInstance().isTrainer() && CommonUtils.isLearningUI(item))
                || (SecurityContext.getInstance().isParticipant() && CommonUtils.isQuizUI(item))
                || (SecurityContext.getInstance().isParticipant() && CommonUtils.isLearningUI(item) && CommonUtils.isParticipantViewMode())) {
            editBtn.setVisibility(View.GONE);
        } else {
            editBtn.setOnClickListener(this);
        }
    }

    private void downloadImage() {
        fileStoreHelper.downloadImage(item.getPhotoUrl(), new FileStoreListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                photoImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure(Exception exception) {
                // TODO
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textToSpeachBtnID:
                String toSpeak = descriptionTextView.getText().toString();
                textToSpeechUtil.speak(toSpeak);
                break;
            case R.id.editBtnID:
                Intent editIntent = new Intent(this.getActivity(), ManageItemActivity.class);
                editIntent.putExtra(AppConstants.MODE, Mode.EDIT);
                editIntent.putExtra(AppConstants.POSITION, position);
                editIntent.putExtra(AppConstants.TITLE_OBJ, title);
                editIntent.putExtra(AppConstants.ITEM_OBJ, item);
                this.getActivity().startActivityForResult(editIntent, RC_EDIT_ITEM);
                break;
        }
    }

    public void onPause() {
        textToSpeechUtil.shutdown();
        super.onPause();
    }

}
