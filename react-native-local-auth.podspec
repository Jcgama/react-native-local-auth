require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "LocalAuth"
  s.version      = package['version']
  s.summary      = "A React Native library for authenticating users with Touch ID"
  s.homepage     = "https://github.com/Jcgama/react-native-local-auth.git"
  s.license      = "MIT"

  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/Jcgama/react-native-local-auth.git" }

  s.source_files  = "*.{h,m}"
  s.dependency "React"
end