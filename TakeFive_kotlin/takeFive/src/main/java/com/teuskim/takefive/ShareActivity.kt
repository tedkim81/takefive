package com.teuskim.takefive

import java.net.URLEncoder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View

import com.facebook.CallbackManager
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.LinkObject
import com.kakao.message.template.TextTemplate
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.teuskim.takefive.common.IGoogleAnalytics

class ShareActivity : BaseGameActivity() {
    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.share_activity)
        FacebookController()
        //		new GooglePlusController();
        KakaoStoryController()
        KakaoLinkController()
        LineLinkController()
        WeChatLinkController()
        WhatsappLinkController()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (callbackManager != null) {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * 페이스북으로 공유하기 관련 클래스
     */
    internal inner class FacebookController {

        private var shareDialog: ShareDialog? = null

        init {
            val btn = findViewById<View>(R.id.btn_facebook)
            if (!ShareDialog.canShow(ShareLinkContent::class.java)) {
                btn.visibility = View.GONE
            } else {
                btn.setOnClickListener {
                    share()
                    googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_FACEBOOK)
                }
                callbackManager = CallbackManager.Factory.create()
                shareDialog = ShareDialog(this@ShareActivity)
            }
        }

        fun share() {
            val content = ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(STORE_URL))
                    .build()
            shareDialog!!.show(content)
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
    internal inner class KakaoStoryController {
        init {
            val btn = findViewById<View>(R.id.btn_kakao_story)
            if (!isInstalled("com.kakao.story")) {
                btn.visibility = View.GONE
            } else {
                btn.setOnClickListener {
                    share()
                    googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_KAKAOSTORY)
                }
            }
        }

        private fun share() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.`package` = "com.kakao.story"
            val s = getString(R.string.link_recommend_text) + "\n" + STORE_URL
            intent.putExtra(Intent.EXTRA_TEXT, s)
            startActivity(Intent.createChooser(intent, "Share with"))
        }
    }

    internal inner class KakaoLinkController {
        init {
            val btn = findViewById<View>(R.id.btn_kakao_link)
            if (!isInstalled("com.kakao.talk")) {
                btn.visibility = View.GONE
            } else {
                btn.setOnClickListener {
                    share()
                    googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_KAKAOLINK)
                }
            }
        }

        fun share() {
            val params = TextTemplate.newBuilder(
                    getString(R.string.link_recommend_text),
                    LinkObject.newBuilder().setAndroidExecutionParams("").build()
            ).setButtonTitle(getString(R.string.kakaolink_button)).build()

            KakaoLinkService.getInstance().sendDefault(this@ShareActivity, params, object : ResponseCallback<KakaoLinkResponse>() {
                override fun onFailure(errorResult: ErrorResult) {
                    showToast(R.string.text_share_kakaolink_fail)
                }

                override fun onSuccess(result: KakaoLinkResponse) {
                    showToast(R.string.text_share_kakaolink_success)
                }
            })
        }
    }

    internal inner class LineLinkController {
        init {
            val btn = findViewById<View>(R.id.btn_line_link)
            if (!isInstalled("jp.naver.line.android")) {
                btn.visibility = View.GONE
            } else {
                btn.setOnClickListener {
                    share()
                    googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_NAVERLINE)
                }
            }
        }

        fun share() {
            try {
                val s = getString(R.string.link_recommend_text) + "\n" + STORE_URL
                val uri = Uri.parse("line://msg/text/" + URLEncoder.encode(s, "UTF-8"))

                val i = Intent(Intent.ACTION_VIEW, uri)
                startActivity(i)

            } catch (e: Exception) {
                showToast(R.string.err_please_retry)
            }

        }
    }

    internal inner class WeChatLinkController {
        /* 4.4 버전 이후 문제가 있는 듯 하다.
		private static final String APP_ID = "wx0a6062c37e206931";
		private IWXAPI api;
		*/

        init {
            val btn = findViewById<View>(R.id.btn_wechat_link)
            if (!isInstalled("com.tencent.mm")) {
                btn.visibility = View.GONE
            } else {
                /*
                api = WXAPIFactory.createWXAPI(ShareActivity.this, APP_ID, true);
                api.registerApp(APP_ID);
                */

                btn.setOnClickListener {
                    share()
                    googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_WECHAT)
                }
            }
        }

        private fun share() {
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

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.`package` = "com.tencent.mm"
            val s = getString(R.string.link_recommend_text) + "\n" + STORE_URL
            intent.putExtra(Intent.EXTRA_TEXT, s)
            startActivity(Intent.createChooser(intent, "Share with"))
        }
    }

    internal inner class WhatsappLinkController {
        init {
            val btn = findViewById<View>(R.id.btn_whatsapp_link)
            if (!isInstalled("com.whatsapp")) {
                btn.visibility = View.GONE
            } else {
                btn.setOnClickListener {
                    share()
                    googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_WHATSAPP)
                }
            }
        }

        private fun share() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.`package` = "com.whatsapp"
            val s = getString(R.string.link_recommend_text) + "\n" + STORE_URL
            intent.putExtra(Intent.EXTRA_TEXT, s)
            startActivity(Intent.createChooser(intent, "Share with"))
        }
    }

    companion object {

        private val STORE_URL = "https://play.google.com/store/apps/details?id=com.teuskim.takefive"
    }

}
