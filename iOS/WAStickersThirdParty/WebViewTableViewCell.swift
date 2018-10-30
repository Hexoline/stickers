//
//  WebViewTableViewCell.swift
//  WAStickersThirdParty
//
//  Created by Grishka on 30/10/2018.
//

import Foundation
import WebKit

class WebViewTableViewCell : UITableViewCell, WKNavigationDelegate {
	
	override init(style: UITableViewCell.CellStyle,
					   reuseIdentifier: String?){
		super.init(style: style, reuseIdentifier: reuseIdentifier);
		
		let webView:WKWebView=WKWebView.init(frame: CGRect(x: 0, y: 0, width: 200, height: 100));
		self.addSubview(webView);
		webView.scrollView.isScrollEnabled=false;
		webView.navigationDelegate=self;
		webView.load(URLRequest.init(url: URL.init(string: String(format:"https://telegram.space/stickers_info/ios?lang=%@", NSLocale.preferredLanguages[0]))!));
		
		webView.translatesAutoresizingMaskIntoConstraints = false
		
		NSLayoutConstraint(item: webView,
						   attribute: .leading,
						   relatedBy: .equal,
						   toItem: self,
						   attribute: .leading,
						   multiplier: 1.0,
						   constant: 0.0).isActive = true
		
		NSLayoutConstraint(item: webView,
						   attribute: .trailing,
						   relatedBy: .equal,
						   toItem: self,
						   attribute: .trailing,
						   multiplier: 1.0,
						   constant: 0.0).isActive = true
		
		NSLayoutConstraint(item: webView,
						   attribute: .top,
						   relatedBy: .equal,
						   toItem: self,
						   attribute: .top,
						   multiplier: 1.0,
						   constant: 0.0).isActive = true
		
		NSLayoutConstraint(item: webView,
						   attribute: .bottom,
						   relatedBy: .equal,
						   toItem: self,
						   attribute: .bottom,
						   multiplier: 1.0,
						   constant: 0.0).isActive = true
	}
	
	required init?(coder aDecoder: NSCoder) {
		fatalError("init(coder:) has not been implemented")
	}
	
	// MARK WKNavigationDelegate
	
	func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
		if navigationAction.navigationType == WKNavigationType.linkActivated {
			UIApplication.shared.openURL(navigationAction.request.url!)
			decisionHandler(WKNavigationActionPolicy.cancel)
			return
		}
		decisionHandler(WKNavigationActionPolicy.allow)
	}
}
