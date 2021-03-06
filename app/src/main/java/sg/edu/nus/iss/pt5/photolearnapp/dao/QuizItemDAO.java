package sg.edu.nus.iss.pt5.photolearnapp.dao;

import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import sg.edu.nus.iss.pt5.photolearnapp.model.Participant;
import sg.edu.nus.iss.pt5.photolearnapp.model.QuizItem;
import sg.edu.nus.iss.pt5.photolearnapp.model.QuizTitle;
import sg.edu.nus.iss.pt5.photolearnapp.model.QuizUserAnswer;

/**
 * Created by Liang Entao on 20/3/18.
 */
public class QuizItemDAO extends BaseEntityDAO<QuizItem> {
    private QuizUserAnswerDAO mChildDao = new QuizUserAnswerDAO();

    public QuizItemDAO() {
        super("quizItems", QuizItem.class);
    }

    public void getItemsByTitle(QuizTitle title, DAOResultListener<Iterable<QuizItem>> resultListener) {
        this.getItemsByTitleId(title.getId(), resultListener);
    }

    public void getItemsByTitleId(String titleId, DAOResultListener<Iterable<QuizItem>> resultListener) {
        ValueEventListener childrenListener = createChildrenEventListener(resultListener);
        mEntityRef.orderByChild("titleId").equalTo(titleId).addValueEventListener(childrenListener);
    }

    @Override
    public void delete(QuizItem obj) {
        this.deleteById(obj.getId());
    }

    @Override
    public void deleteById(String objId) {
        DAOResultListener<Iterable<QuizUserAnswer>> resultListener = new DAOResultListener<Iterable<QuizUserAnswer>>() {
            @Override
            public void OnDAOReturned(Iterable<QuizUserAnswer> items) {
                mChildDao.delete(items);
            }
        };
        mChildDao.getAnswersByItemId(objId, resultListener);
        super.deleteById(objId);
    }

    @Override
    public void delete(Iterable<QuizItem> objects) {
        for (QuizItem obj : objects) {
            this.delete(obj);
        }
    }

    @Override
    public void deleteById(Iterable<String> objIds) {
        for (String objId : objIds) {
            this.deleteById(objId);
        }
    }

    public void deleteAnswersByQuizTitleUserId(Participant participant, QuizTitle title) {
        this.deleteAnswersByQuizTitleUserId(participant.getId(), title.getId());
    }

    public void deleteAnswersByQuizTitleUserId(final String userId, String quizTitleId) {
        DAOResultListener<Iterable<QuizItem>> itemListener = new DAOResultListener<Iterable<QuizItem>>() {
            @Override
            public void OnDAOReturned(Iterable<QuizItem> list) {
                for (QuizItem item : list) {
                    DAOResultListener<Iterable<QuizUserAnswer>> answerListener = new DAOResultListener<Iterable<QuizUserAnswer>>() {
                        @Override
                        public void OnDAOReturned(Iterable<QuizUserAnswer> objects) {
                            for (QuizUserAnswer obj : objects) {
                                mChildDao.delete(obj);
                            }
                        }
                    };
                    mChildDao.getAnswersByQuizUserId(userId, item.getId(), answerListener);
                }
            }
        };
        this.getItemsByTitleId(quizTitleId, itemListener);
    }
}
