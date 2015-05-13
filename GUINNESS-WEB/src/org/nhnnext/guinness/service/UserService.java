package org.nhnnext.guinness.service;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.nhnnext.guinness.dao.ConfirmDao;
import org.nhnnext.guinness.dao.UserDao;
import org.nhnnext.guinness.exception.AlreadyExistedUserIdException;
import org.nhnnext.guinness.exception.FailedLoginException;
import org.nhnnext.guinness.exception.SendMailException;
import org.nhnnext.guinness.exception.UserUpdateException;
import org.nhnnext.guinness.model.User;
import org.nhnnext.guinness.util.RandomFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Resource
	private UserDao userDao;
	@Resource
	private ConfirmDao confirmDao;
	@Resource
	private JavaMailSender javaMailSender;
	
	public void join(User user) throws AlreadyExistedUserIdException, SendMailException {
		User existedUser = createUser(user);
		createConfirm(user, existedUser);
	}
	
	private User createUser(User user) throws AlreadyExistedUserIdException {
//		TODO 코드리뷰
//		if(null != userDao.findUserByUserId(user.getUserId())) {
		if(userDao.findUserByUserId(user.getUserId()) != null) {
			throw new AlreadyExistedUserIdException("이미 존재하는 계정입니다.");
		}
		userDao.createUser(user);
		return userDao.findUserByUserId(user.getUserId());
	}

	private void createConfirm(User user, User existedUser) throws SendMailException {
		if("R".equals(existedUser.getStatus())) {
			confirmDao.deleteConfirmByUserId(user.getUserId());
		}
		String keyAddress = createKeyAddress();
		confirmDao.createConfirm(keyAddress, user.getUserId());
		sendMail(keyAddress, user.getUserId());
	}
	
	private String createKeyAddress() {
		String keyAddress = RandomFactory.getRandomId(10);
		if(confirmDao.isExistKeyAddress(keyAddress)) {
			return createKeyAddress();
		}
		return keyAddress;
	}
	
	public User confirm(String keyAddress) {
		String userId = confirmDao.findUserIdByKeyAddress(keyAddress);
		userDao.updateUserState(userId, "E");
		confirmDao.deleteConfirmByKeyAddress(keyAddress);
		return userDao.findUserByUserId(userId);
	}
	
	public User login(String userId, String userPassword) throws FailedLoginException {
		User user = userDao.findUserByUserId(userId);
		if (user == null || !user.isCorrectPassword(userPassword) || !user.checkStatus("E")) {
			throw new FailedLoginException();
		}
		return user;
	}

	private void sendMail(String keyAddress, String userId) throws SendMailException  {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
			messageHelper.setTo(userId);
			messageHelper.setFrom("hakimaru@naver.com");
			messageHelper.setSubject("환영합니다. 페이퍼민트 가입 인증 메일입니다.");
			messageHelper.setText("<a href='http://localhost:8080/user/confirm/" + keyAddress + "'> 페이퍼민트 시작하기 </a>", true);
			javaMailSender.send(message);
		} catch (MessagingException | NullPointerException | MailAuthenticationException e) {
			throw new SendMailException(e.getClass().getSimpleName());
		}
	}
	public void update(User user, String userOldPassword, Model model, String rootPath, MultipartFile profileImage) throws UserUpdateException {
		//TODO update 방식에 변경 소요 있음.
		User prevUser = userDao.findUserByUserId(user.getUserId());
		if (!prevUser.isCorrectPassword(userOldPassword)) {
			throw new UserUpdateException("비밀번호가 일치하지 않습니다.");
		}
		try {
			user.setUserImage(prevUser.getUserImage());
			if (!profileImage.isEmpty()) {
				String fileName = user.getUserId();
				profileImage.transferTo(new File(rootPath + "img/profile/" + fileName));
				user.setUserImage(fileName);
				userDao.updateUser(user);
			}
		} catch (IllegalStateException | IOException | DataIntegrityViolationException e) {
			e.printStackTrace();
			throw new UserUpdateException("잘못된 형식입니다.");
		}
	}
}
