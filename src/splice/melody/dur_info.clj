;    Copyright (C) 2017-2018  Joseph Fosco. All Rights Reserved
;
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns splice.melody.dur-info)

(defrecord DurInfo [dur-millis dur-beats dur-base-millis])

(defn create-dur-info
  [& {:keys [dur-millis dur-beats dur-base-millis] :or
      {dur-millis nil dur-beats nil dur-base-millis nil}}]
  (DurInfo. dur-millis dur-beats dur-base-millis)
  )

(defn get-dur-millis-from-dur-info
  [dur-info]
  (:dur-millis dur-info))

(defn get-dur-beats-from-dur-info
  [dur-info]
  (:dur-beats dur-info))

(defn get-dur-base-millis-from-dur-info
  [dur-info]
  (:dur-base-millis dur-info))
