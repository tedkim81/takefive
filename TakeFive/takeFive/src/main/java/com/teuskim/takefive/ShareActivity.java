package com.teuskim.takefive;

import java.net.URLEncoder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

public class ShareActivity extends BaseGameActivity {
	
	private static final String STORE_URL = "https://play.google.com/store/apps/details?id=com.teuskim.takefive";
	private CallbackManager callbackManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_activity);
		new FacebookController();
//		new GooglePlusController();
		new KakaoStoryController();
		new KakaoLinkController();
		new LineLinkController();
		new WeChatLinkController();
		new WhatsappLinkController();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (callbackManager != null) {
			callbackManager.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * 페이스북으로 공유하기 관련 클래스
	 */
	class FacebookController {

		private ShareDialog shareDialog;

		FacebookController() {
			View btn = findViewById(R.id.btn_facebook);
			if (!ShareDialog.canShow(ShareLinkContent.class)) {
				btn.setVisibility(View.GONE);
				return;
			}
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					share();
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_FACEBOOK);
				}
			});
			callbackManager = CallbackManager.Factory.create();
			shareDialog = new ShareDialog(ShareActivity.this);
		}

		public void share() {
			ShareLinkContent content = new ShareLinkContent.Builder()
					.setContentUrl(Uri.parse(STORE_URL))
					.build();
			shareDialog.show(content);
		}
	}
	
	/**
	 * 구글플러스로 공유하기 관련 클래스
	 */
//	class GooglePlusController {
//
//		GooglePlusController() {
//			findViewById(R.id.btn_googleplus).setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					share();
//					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_GOOGLEPLUS);
//				}
//			});
//		}
//
//		public void share() {
//			Intent intent = new PlusShare.Builder(ShareActivity.this)
//							.setType("text/plain")
//							.setText(getString(R.string.link_recommend_text))
//							.setContentUrl(Uri.parse(STORE_URL))
//							.getIntent();
//			startActivity(intent);
//		}
//	}

	/**
	 * 카카오스토리로 공유하기 관련 클래스
	 */
	class KakaoStoryController {
		
		KakaoStoryController() {
			View btn = findViewById(R.id.btn_kakao_story);
			if (!isInstalled("com.kakao.story")) {
				btn.setVisibility(View.GONE);
				return;
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					share();
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_KAKAOSTORY);
				}
			});
		}
		
		private void share() {
			Intent intent = new Intent(Intent.ACTION_SEND);
		    intent.setType("text/plain");
		    intent.setPackage("com.kakao.story");
		    String s = getString(R.string.link_recommend_text)+"\n"+STORE_URL;
		    intent.putExtra(Intent.EXTRA_TEXT, s);
			startActivity(Intent.createChooser(intent, "Share with"));
		}
	}
	
	class KakaoLinkController {
		
		KakaoLinkController() {
			View btn = findViewById(R.id.btn_kakao_link);
			if (!isInstalled("com.kakao.talk")) {
				btn.setVisibility(View.GONE);
				return;
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					share();
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_KAKAOLINK);
				}
			});
		}
		
		public void share() {
			TextTemplate params = TextTemplate.newBuilder(
					getString(R.string.link_recommend_text),
					LinkObject.newBuilder().setAndroidExecutionParams("").build()
			).setButtonTitle(getString(R.string.kakaolink_button)).build();

			KakaoLinkService.getInstance().sendDefault(ShareActivity.this, params, new ResponseCallback<KakaoLinkResponse>() {
				@Override
				public void onFailure(ErrorResult errorResult) {
					showToast(R.string.text_share_kakaolink_fail);
				}

				@Override
				public void onSuccess(KakaoLinkResponse result) {
					showToast(R.string.text_share_kakaolink_success);
				}
			});
		}
	}
	
	class LineLinkController {
		
		LineLinkController() {
			View btn = findViewById(R.id.btn_line_link);
			if (!isInstalled("jp.naver.line.android")) {
				btn.setVisibility(View.GONE);
				return;
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					share();
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_NAVERLINE);
				}
			});
		}
		
		public void share() {
			try {
				String s = getString(R.string.link_recommend_text)+"\n"+STORE_URL;
				Uri uri = Uri.parse("line://msg/text/"+URLEncoder.encode(s, "UTF-8"));
				
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(i);
				
			} catch (Exception e) {
				showToast(R.string.err_please_retry);
			}
		}
	}
	
	class WeChatLinkController {

		/* 4.4 버전 이후 문제가 있는 듯 하다.
		private static final String APP_ID = "wx0a6062c37e206931";
		private IWXAPI api;
		*/
		
		WeChatLinkController() {
			View btn = findViewById(R.id.btn_wechat_link);
			if (!isInstalled("com.tencent.mm")) {
				btn.setVisibility(View.GONE);
				return;
			}
			/*
			api = WXAPIFactory.createWXAPI(ShareActivity.this, APP_ID, true);
			api.registerApp(APP_ID);
			*/
			
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					share();
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_WECHAT);
				}
			});
		}
		
		private void share() {
			/*
			WXTextObject textObj = new WXTextObject();
			textObj.text = getString(R.string.link_recommend_text)+"\n"+STORE_URL;
			
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = textObj;
			msg.description = getString(R.string.text_wechat_link_desc);
			
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = String.valueOf(System.currentTimeMillis());
			req.message = msg;
			
			api.sendReq(req);
			*/
			
			Intent intent = new Intent(Intent.ACTION_SEND);
		    intent.setType("text/plain");
		    intent.setPackage("com.tencent.mm");
		    String s = getString(R.string.link_recommend_text)+"\n"+STORE_URL;
		    intent.putExtra(Intent.EXTRA_TEXT, s);
			startActivity(Intent.createChooser(intent, "Share with"));
		}
	}
	
	class WhatsappLinkController {
		
		WhatsappLinkController() {
			View btn = findViewById(R.id.btn_whatsapp_link);
			if (!isInstalled("com.whatsapp")) {
				btn.setVisibility(View.GONE);
				return;
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					share();
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_WHATSAPP);
				}
			});
		}
		
		private void share() {
			Intent intent = new Intent(Intent.ACTION_SEND);
		    intent.setType("text/plain");
		    intent.setPackage("com.whatsapp");
		    String s = getString(R.string.link_recommend_text)+"\n"+STORE_URL;
		    intent.putExtra(Intent.EXTRA_TEXT, s);
			startActivity(Intent.createChooser(intent, "Share with"));
		}
	}
	
}
