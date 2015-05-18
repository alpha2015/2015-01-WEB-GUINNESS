package org.nhnnext.guinness.dao;

import java.util.List;
import java.util.Map;

import org.nhnnext.guinness.model.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class AlarmDao extends JdbcDaoSupport {
	private static final Logger logger = LoggerFactory.getLogger(AlarmDao.class);

	public void create(Alarm alarm) {
		String sql = "insert into ALARMS (alarmId, calleeId, callerId, noteId, invitedGroupId, alarmStatus, alarmCreateDate) values(?, ?, ?, ?, ?, ?, default)";
		getJdbcTemplate().update(sql, alarm.getAlarmId(), alarm.getReader().getUserId(), alarm.getWriter().getUserId(), alarm.getNote().getNoteId(), alarm.getGroup().getGroupId(),
				alarm.getAlarmStatus());
	}

	public boolean isExistAlarmId(String alarmId) {
		String sql = "select count(1) from ALARMS where alarmId = ?";
		logger.debug("{}", ""+getJdbcTemplate().queryForObject(sql, Integer.class, alarmId));
		if (getJdbcTemplate().queryForObject(sql, Integer.class, alarmId) == 0) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public List<Map<String, Object>> list(String calleeId) {
		//String sql = "select A.*, U.userName, G.groupName from ALARMS as A, USERS as U, NOTES as N, GROUPS as G where A.calleeId=? and A.callerId=U.userId and A.noteId = N.noteId and N.groupId = G.groupId order by A.alarmCreateDate desc;";
		String sql = "select A.*, U.userName, G.groupName from ALARMS as A, USERS as U, NOTES as N, GROUPS as G where A.calleeId= ? and A.callerId=U.userId and A.noteId = N.noteId and ((A.alarmStatus='G' and A.invitedGroupId=G.groupId) or ((A.alarmStatus='N' or A.alarmStatus='C') and N.groupId = G.groupId)) order by A.alarmCreateDate desc;";
		return getJdbcTemplate().queryForList(sql, calleeId);
	}

	public void delete(String alarmId) {
		String sql = "delete from ALARMS where alarmId = ?";
		getJdbcTemplate().update(sql, alarmId);
	}

	public List<Map<String, Object>> readNoteAlarm(String sessionUserId) {
		String sql = "select groupId, count(*) as groupAlarmCount from ALARMS as A, NOTES as N where A.alarmStatus = 'N' and A.calleeId =? and N.noteId = A.noteId GROUP BY groupId order by groupId;";
		return getJdbcTemplate().queryForList(sql, sessionUserId);
	}

	public boolean checkStandbyJoinGroup(String userId, String groupId) {
		String sql = "select count(1) from ALARMS where calleeId = ? and invitedGroupId = ?";
		if (getJdbcTemplate().queryForObject(sql, Integer.class, userId, groupId) == 0) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
}
