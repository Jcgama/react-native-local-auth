/**
 * Mostly a copy of https://github.com/naoufal/react-native-touch-id
 * @providesModule LocalAuth
 * @flow
 */
'use strict'

import { createError } from './error'
import Errors from './data/errors'
import { NativeModules } from 'react-native'

const { RNLocalAuth } = NativeModules

module.exports = {

  hasTouchID() {
    return new Promise((resolve, reject) => {
      RNLocalAuth.hasTouchID(error => reject(error), success => resolve(success))
    })
  },
 
  isDeviceSecure() {
    return RNLocalAuth.isDeviceSecure()
  },

  authenticate(opts) {
    return RNLocalAuth.authenticate(opts)
      .catch(err => {
        err.name = err.code
        throw err
      })
  }
}
