(ns status-im2.contexts.chat.messages.navigation.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [re-frame.db]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.platform :as platform]
            [status-im2.contexts.chat.messages.navigation.style :as style]
            [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [status-im2.common.home.actions.view :as actions]))

(defn f-navigation-view
  [{:keys [scroll-y shared-all-loaded?]}]
  (let [{:keys [group-chat chat-id chat-name emoji
                chat-type]
         :as   chat}             (rf/sub [:chats/current-chat-chat-view])
        all-loaded?              @shared-all-loaded?
        display-name             (if (= chat-type constants/one-to-one-chat-type)
                                   (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                                   (str emoji " " chat-name))
        online?                  (rf/sub [:visibility-status-updates/online? chat-id])
        contact                  (when-not group-chat
                                   (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path               (when-not (empty? (:images contact))
                                   (rf/sub [:chats/photo-path chat-id]))
        opacity-animation        (reanimated/interpolate scroll-y
                                                         [style/navigation-bar-height
                                                          (+ style/navigation-bar-height 30)]
                                                         [0 1]
                                                         {:extrapolateLeft  "clamp"
                                                          :extrapolateRight "extend"})
        banner-opacity-animation (reanimated/interpolate scroll-y
                                                         [(+ style/navigation-bar-height 50)
                                                          (+ style/navigation-bar-height 100)]
                                                         [0 1]
                                                         {:extrapolateLeft  "clamp"
                                                          :extrapolateRight "extend"})
        translate-animation      (reanimated/interpolate scroll-y
                                                         [(+ style/navigation-bar-height 25)
                                                          (+ style/navigation-bar-height 100)]
                                                         [50 0]
                                                         {:extrapolateLeft  "clamp"
                                                          :extrapolateRight "clamp"})
        title-opacity-animation  (reanimated/interpolate scroll-y
                                                         [0 50]
                                                         [0 1]
                                                         {:extrapolateLeft  "clamp"
                                                          :extrapolateRight "clamp"})]
    [rn/view {:style style/navigation-view}
     [reanimated/view
      {:style (style/animated-background-view all-loaded? opacity-animation)}]

     [reanimated/view {:style (style/animated-blur-view all-loaded? opacity-animation)}
      [blur/view
       {:blur-amount 20
        :blur-type   (colors/theme-colors :light :dark)
        :blur-radius (if platform/ios? 20 10)
        :style       {:flex 1}}]]

     [rn/view
      [rn/view {:style style/header-container}
       [rn/touchable-opacity
        {:active-opacity      1
         :on-press            #(do
                                 (rf/dispatch [:chat/close])
                                 (rf/dispatch [:navigate-back]))
         :accessibility-label :back-button
         :style               (style/button-container {:margin-left 20})}
        [quo/icon :i/arrow-left
         {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
       [reanimated/view
        {:style (style/animated-header all-loaded? translate-animation title-opacity-animation)}
        [rn/view {:style style/header-content-container}
         (when-not group-chat
           [rn/view {:style style/header-avatar-container}
            [quo/user-avatar
             {:full-name       display-name
              :online?         online?
              :profile-picture photo-path
              :size            :small}]])
         [rn/view {:style style/header-text-container}
          [rn/view {:style {:flex-direction :row}}
           [quo/text
            {:weight          :semi-bold
             :size            :paragraph-1
             :number-of-lines 1
             :style           (style/header-display-name)}
            display-name]]
          (when online?
            [quo/text
             {:number-of-lines 1
              :weight          :regular
              :size            :paragraph-2
              :style           (style/header-online)}
             (i18n/label :t/online)])]]]
       [rn/touchable-opacity
        {:active-opacity      1
         :style               (style/button-container {:margin-right 20})
         :accessibility-label :options-button
         :on-press            (fn []
                                (rf/dispatch [:dismiss-keyboard])
                                (rf/dispatch [:show-bottom-sheet
                                              {:content (fn [] [actions/chat-actions chat true])}]))}
        [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]]

      [pin.banner/banner
       {:chat-id           chat-id
        :opacity-animation banner-opacity-animation
        :all-loaded?       all-loaded?
        :top-offset        style/navigation-bar-height}]]]))

(defn navigation-view
  [props]
  [:f> f-navigation-view props])
