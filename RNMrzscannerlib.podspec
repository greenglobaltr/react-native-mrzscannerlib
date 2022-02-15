require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = 'RNMrzscannerlib'
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platform     = :ios, "7.0"

  s.source       = { :git => "https://github.com/author/RNMrzscannerlib.git", :tag => package['version'] }
  s.source_files  = 'ios/*.{h,m}'
  s.vendored_frameworks = 'ios/MRZScannerSDK.framework'

  s.dependency 'React'
end